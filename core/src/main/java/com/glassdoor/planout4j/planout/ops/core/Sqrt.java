package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;
import java.util.Map;

public class Sqrt extends PlanOutOpUnary<Number> {

  public Sqrt(final Map<String, Object> args) {
    super(args);
  }

  @Override
  protected Number unaryExecute(final Object value) throws IllegalArgumentException {
    final Double val = getNumber(value, "value").doubleValue();
    if (val < 0) {
      throw new IllegalArgumentException();
    } else {
      return Helper.wrapNumber(Math.sqrt(val), false);
    }
  }

}
