package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;
import java.util.Map;

public class Exp extends PlanOutOpUnary<Number> {

  public Exp(final Map<String, Object> args) {
    super(args);
  }

  @Override
  protected Number unaryExecute(final Object value) {
    final Double val = getNumber(value, "value").doubleValue();
    return Helper.wrapNumber(Math.exp(val), false);
  }

}
