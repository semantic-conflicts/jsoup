package org.jsoup.select;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.TokenQueue;
/** 
 * Parses a CSS selector into an Evaluator tree.
 */
class QueryParser {
  public static String[] combinators={",",">","+","~"," "};
  public static String[] AttributeEvals=new String[]{"=","!=","^=","$=","*=","~="};
  public TokenQueue tq;
  public String query;
  public List<Evaluator> evals=new ArrayList<Evaluator>();
  /** 
 * Create a new QueryParser.
 * @param query CSS query
 */
  public QueryParser(  String query){
    this.query=query;
    this.tq=new TokenQueue(query);
  }
  /** 
 * Parse a CSS query into an Evaluator.
 * @param query CSS query
 * @return Evaluator
 */
  public static Evaluator parse(  String query){
    QueryParser p=new QueryParser(query);
    return p.parse();
  }
  /** 
 * Parse the query
 * @return Evaluator
 */
  Evaluator parse(){
    tq.consumeWhitespace();
    if (tq.matchesAny(combinators)) {
      evals.add(new StructuralEvaluator.Root());
      combinator(tq.consume());
    }
 else {
      findElements();
    }
    while (!tq.isEmpty()) {
      boolean seenWhite=tq.consumeWhitespace();
      if (tq.matchesAny(combinators)) {
        combinator(tq.consume());
      }
 else       if (seenWhite) {
        combinator(' ');
      }
 else {
        findElements();
      }
    }
    if (evals.size() == 1)     return evals.get(0);
    return new CombiningEvaluator.And(evals);
  }
  public void combinator(  char combinator){
    tq.consumeWhitespace();
    String subQuery=consumeSubQuery();
    Evaluator rootEval;
    Evaluator currentEval;
    Evaluator newEval=parse(subQuery);
    boolean replaceRightMost=false;
    if (evals.size() == 1) {
      rootEval=currentEval=evals.get(0);
      if (rootEval instanceof CombiningEvaluator.Or && combinator != ',') {
        currentEval=((CombiningEvaluator.Or)currentEval).rightMostEvaluator();
        replaceRightMost=true;
      }
    }
 else {
      rootEval=currentEval=new CombiningEvaluator.And(evals);
    }
    evals.clear();
    if (combinator == '>')     currentEval=new CombiningEvaluator.And(newEval,new StructuralEvaluator.ImmediateParent(currentEval));
 else     if (combinator == ' ')     currentEval=new CombiningEvaluator.And(newEval,new StructuralEvaluator.Parent(currentEval));
 else     if (combinator == '+')     currentEval=new CombiningEvaluator.And(newEval,new StructuralEvaluator.ImmediatePreviousSibling(currentEval));
 else     if (combinator == '~')     currentEval=new CombiningEvaluator.And(newEval,new StructuralEvaluator.PreviousSibling(currentEval));
 else     if (combinator == ',') {
      CombiningEvaluator.Or or;
      if (currentEval instanceof CombiningEvaluator.Or) {
        or=(CombiningEvaluator.Or)currentEval;
        or.add(newEval);
      }
 else {
        or=new CombiningEvaluator.Or();
        or.add(currentEval);
        or.add(newEval);
      }
      currentEval=or;
    }
 else     throw new Selector.SelectorParseException("Unknown combinator: " + combinator);
    if (replaceRightMost)     ((CombiningEvaluator.Or)rootEval).replaceRightMostEvaluator(currentEval);
 else     rootEval=currentEval;
    evals.add(rootEval);
  }
  public String consumeSubQuery(){
    StringBuilder sq=new StringBuilder();
    while (!tq.isEmpty()) {
      if (tq.matches("("))       sq.append("(").append(tq.chompBalanced('(',')')).append(")");
 else       if (tq.matches("["))       sq.append("[").append(tq.chompBalanced('[',']')).append("]");
 else       if (tq.matchesAny(combinators))       break;
 else       sq.append(tq.consume());
    }
    return sq.toString();
  }
  public void findElements(){
    if (tq.matchChomp("#"))     byId();
 else     if (tq.matchChomp("."))     byClass();
 else     if (tq.matchesWord())     byTag();
 else     if (tq.matches("["))     byAttribute();
 else     if (tq.matchChomp("*"))     allElements();
 else     if (tq.matchChomp(":lt("))     indexLessThan();
 else     if (tq.matchChomp(":gt("))     indexGreaterThan();
 else     if (tq.matchChomp(":eq("))     indexEquals();
 else     if (tq.matches(":has("))     has();
 else     if (tq.matches(":contains("))     contains(false);
 else     if (tq.matches(":containsOwn("))     contains(true);
 else     if (tq.matches(":matches("))     matches(false);
 else     if (tq.matches(":matchesOwn("))     matches(true);
 else     if (tq.matches(":not("))     not();
 else     if (tq.matchChomp(":nth-child("))     cssNthChild(false,false);
 else     if (tq.matchChomp(":nth-last-child("))     cssNthChild(true,false);
 else     if (tq.matchChomp(":nth-of-type("))     cssNthChild(false,true);
 else     if (tq.matchChomp(":nth-last-of-type("))     cssNthChild(true,true);
 else     if (tq.matchChomp(":first-child"))     evals.add(new Evaluator.IsFirstChild());
 else     if (tq.matchChomp(":last-child"))     evals.add(new Evaluator.IsLastChild());
 else     if (tq.matchChomp(":first-of-type"))     evals.add(new Evaluator.IsFirstOfType());
 else     if (tq.matchChomp(":last-of-type"))     evals.add(new Evaluator.IsLastOfType());
 else     if (tq.matchChomp(":only-child"))     evals.add(new Evaluator.IsOnlyChild());
 else     if (tq.matchChomp(":only-of-type"))     evals.add(new Evaluator.IsOnlyOfType());
 else     if (tq.matchChomp(":empty"))     evals.add(new Evaluator.IsEmpty());
 else     if (tq.matchChomp(":root"))     evals.add(new Evaluator.IsRoot());
 else     throw new Selector.SelectorParseException("Could not parse query '%s': unexpected token at '%s'",query,tq.remainder());
  }
  public void byId(){
    String id=tq.consumeCssIdentifier();
    Validate.notEmpty(id);
    evals.add(new Evaluator.Id(id));
  }
  public void byClass(){
    String className=tq.consumeCssIdentifier();
    Validate.notEmpty(className);
    evals.add(new Evaluator.Class(className.trim()));
  }
  public void byTag(){
    String tagName=tq.consumeElementSelector();
    Validate.notEmpty(tagName);
    if (tagName.contains("|"))     tagName=tagName.replace("|",":");
    evals.add(new Evaluator.Tag(tagName.trim()));
  }
  public void byAttribute(){
    TokenQueue cq=new TokenQueue(tq.chompBalanced('[',']'));
    String key=cq.consumeToAny(AttributeEvals);
    Validate.notEmpty(key);
    cq.consumeWhitespace();
    if (cq.isEmpty()) {
      if (key.startsWith("^"))       evals.add(new Evaluator.AttributeStarting(key.substring(1)));
 else       evals.add(new Evaluator.Attribute(key));
    }
 else {
      if (cq.matchChomp("="))       evals.add(new Evaluator.AttributeWithValue(key,cq.remainder()));
 else       if (cq.matchChomp("!="))       evals.add(new Evaluator.AttributeWithValueNot(key,cq.remainder()));
 else       if (cq.matchChomp("^="))       evals.add(new Evaluator.AttributeWithValueStarting(key,cq.remainder()));
 else       if (cq.matchChomp("$="))       evals.add(new Evaluator.AttributeWithValueEnding(key,cq.remainder()));
 else       if (cq.matchChomp("*="))       evals.add(new Evaluator.AttributeWithValueContaining(key,cq.remainder()));
 else       if (cq.matchChomp("~="))       evals.add(new Evaluator.AttributeWithValueMatching(key,Pattern.compile(cq.remainder())));
 else       throw new Selector.SelectorParseException("Could not parse attribute query '%s': unexpected token at '%s'",query,cq.remainder());
    }
  }
  public void allElements(){
    evals.add(new Evaluator.AllElements());
  }
  public void indexLessThan(){
    evals.add(new Evaluator.IndexLessThan(consumeIndex()));
  }
  public void indexGreaterThan(){
    evals.add(new Evaluator.IndexGreaterThan(consumeIndex()));
  }
  public void indexEquals(){
    evals.add(new Evaluator.IndexEquals(consumeIndex()));
  }
  public static Pattern NTH_AB=Pattern.compile("((\\+|-)?(\\d+)?)n(\\s*(\\+|-)?\\s*\\d+)?",Pattern.CASE_INSENSITIVE);
  public static Pattern NTH_B=Pattern.compile("(\\+|-)?(\\d+)");
  public void cssNthChild(  boolean backwards,  boolean ofType){
    String argS=tq.chompTo(")").trim().toLowerCase();
    Matcher mAB=NTH_AB.matcher(argS);
    Matcher mB=NTH_B.matcher(argS);
    final int a, b;
    if ("odd".equals(argS)) {
      a=2;
      b=1;
    }
 else     if ("even".equals(argS)) {
      a=2;
      b=0;
    }
 else     if (mAB.matches()) {
      a=mAB.group(3) != null ? Integer.parseInt(mAB.group(1).replaceFirst("^\\+","")) : 1;
      b=mAB.group(4) != null ? Integer.parseInt(mAB.group(4).replaceFirst("^\\+","")) : 0;
    }
 else     if (mB.matches()) {
      a=0;
      b=Integer.parseInt(mB.group().replaceFirst("^\\+",""));
    }
 else {
      throw new Selector.SelectorParseException("Could not parse nth-index '%s': unexpected format",argS);
    }
    if (ofType)     if (backwards)     evals.add(new Evaluator.IsNthLastOfType(a,b));
 else     evals.add(new Evaluator.IsNthOfType(a,b));
 else {
      if (backwards)       evals.add(new Evaluator.IsNthLastChild(a,b));
 else       evals.add(new Evaluator.IsNthChild(a,b));
    }
  }
  public int consumeIndex(){
    String indexS=tq.chompTo(")").trim();
    Validate.isTrue(StringUtil.isNumeric(indexS),"Index must be numeric");
    return Integer.parseInt(indexS);
  }
  public void has(){
    tq.consume(":has");
    String subQuery=tq.chompBalanced('(',')');
    Validate.notEmpty(subQuery,":has(el) subselect must not be empty");
    evals.add(new StructuralEvaluator.Has(parse(subQuery)));
  }
  public void contains(  boolean own){
    tq.consume(own ? ":containsOwn" : ":contains");
    String searchText=TokenQueue.unescape(tq.chompBalanced('(',')'));
    Validate.notEmpty(searchText,":contains(text) query must not be empty");
    if (own)     evals.add(new Evaluator.ContainsOwnText(searchText));
 else     evals.add(new Evaluator.ContainsText(searchText));
  }
  public void matches(  boolean own){
    tq.consume(own ? ":matchesOwn" : ":matches");
    String regex=tq.chompBalanced('(',')');
    Validate.notEmpty(regex,":matches(regex) query must not be empty");
    if (own)     evals.add(new Evaluator.MatchesOwn(Pattern.compile(regex)));
 else     evals.add(new Evaluator.Matches(Pattern.compile(regex)));
  }
  public void not(){
    tq.consume(":not");
    String subQuery=tq.chompBalanced('(',')');
    Validate.notEmpty(subQuery,":not(selector) subselect must not be empty");
    evals.add(new StructuralEvaluator.Not(parse(subQuery)));
  }
  public QueryParser(){
  }
}
