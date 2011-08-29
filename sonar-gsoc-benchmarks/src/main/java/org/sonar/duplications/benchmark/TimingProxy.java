/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.benchmark;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Allows to measure time of execution of methods.
 */
public class TimingProxy implements InvocationHandler {

  private Map<String, Long> timings = Maps.newHashMap();
  private Object delegate;

  public static <E> E newInstance(Object obj) {
    return (E) java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(), getAllInterfaces(obj.getClass()), new TimingProxy(obj));
  }

  public static TimingProxy getHandlerFor(Object proxy) {
    return (TimingProxy) Proxy.getInvocationHandler(proxy);
  }

  private static Class[] getAllInterfaces(Class cls) {
    List<Class> result = Lists.newArrayList();
    while (cls != null) { // up to Object by class hierarchy
      for (Class i : cls.getInterfaces()) {
        result.add(i);
      }
      cls = cls.getSuperclass();
    }
    return result.toArray(new Class[result.size()]);
  }

  private TimingProxy(Object delegate) {
    this.delegate = delegate;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result;
    long start = System.currentTimeMillis();
    try {
      result = method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      throw new RuntimeException("Unexpected invocation exception: " + e.getMessage());
    } finally {
      Long sum = timings.get(method.getName());
      if (sum == null) {
        sum = 0L;
      }
      timings.put(method.getName(), sum + System.currentTimeMillis() - start);
    }
    return result;
  }

  public void printTimings() {
    long total = 0L;
    for (Long l : timings.values()) {
      total += l;
    }

    System.out.println("Timings for " + delegate);
    for (Map.Entry<String, Long> entry : timings.entrySet()) {
      long value = entry.getValue();
      System.out.println(String.format(Locale.ENGLISH, "%20s : %6.2f ( %6.2f )", entry.getKey(), value / 1000.0, value * 100.0 / total));
    }
    System.out.println(String.format(Locale.ENGLISH, "%20s : %6.2f", "Total", total / 1000.0));
  }

}