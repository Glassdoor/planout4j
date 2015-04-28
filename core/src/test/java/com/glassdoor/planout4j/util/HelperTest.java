package com.glassdoor.planout4j.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class HelperTest {

   @Before
   public void setUp() throws Exception {}

   @Test
   public void should_cast_when_type_matches() {
      String s1 = "Hello";
      Object s1Obj = s1;
      String s1Cast = Helper.cast(s1Obj);
      assertThat(s1Cast).isEqualTo(s1);
      
      int i1 = 11;
      Object i1Obj = i1;
      int i1Cast = Helper.cast(i1Obj);
      assertThat(i1Cast).isEqualTo(i1);
   }

   @Test(expected=ClassCastException.class)
   public void should_not_cast_to_double_when_type_is_int() {
      int i2 = 11;
      Object i2Obj = i2;
      double d2Cast = Helper.cast(i2Obj);
      assertThat(d2Cast).isEqualTo(i2);
   }

   @Test(expected=ClassCastException.class)
   public void should_not_cast_to_int_when_type_is_double() {
      double d3 = 11;
      Object d3Obj = d3;
      int i3Cast = Helper.cast(d3Obj);
      assertThat(i3Cast).isEqualTo(d3);
   }

}
