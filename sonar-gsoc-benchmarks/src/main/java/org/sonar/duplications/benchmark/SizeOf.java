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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Calculates approximate size of an Object.
 * See <a href="http://devblog.streamy.com/2009/07/24/determine-size-of-java-object-class/">"HOWTO: Determine the size of a Java Object or Class"</a>.
 */
public final class SizeOf {

  /**
   * 4 bytes on a 32 bit system and 8 bytes on a 64 bit system.
   */
  private final long ref;

  private final Map<Object, Void> visited = new IdentityHashMap<Object, Void>();
  private final Stack<Object> stack = new Stack<Object>();

  private SizeOf(long ref) {
    this.ref = ref;
  }

  public static long sizeOf(Object obj) {
    return new SizeOf(4).estimate(obj);
  }

  public static long sizeOfOn64(Object obj) {
    return new SizeOf(8).estimate(obj);
  }

  public synchronized long estimate(Object obj) {
    long result = estimateObject(obj);
    while (!stack.isEmpty()) {
      result += estimateObject(stack.pop());
    }
    visited.clear();
    return result;
  }

  private long estimateObject(Object obj) {
    if (isSkipObject(obj)) {
      return 0;
    }
    visited.put(obj, null);
    Class cls = obj.getClass();
    if (cls.isArray()) {
      return estimateArray(obj);
    }
    long result = 2 * ref;
    while (cls != null) { // up to Object by class hierarchy
      Field[] fields = cls.getDeclaredFields();
      for (Field field : fields) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        if (field.getType().isPrimitive()) {
          result += primitiveSizes.get(field.getType());
        } else {
          result += ref;
          field.setAccessible(true);
          try {
            Object value = field.get(obj);
            if (value != null) {
              stack.add(value);
            }
          } catch (IllegalAccessException ex) {
            assert false;
          }
        }
      }
      cls = cls.getSuperclass();
    }
    return align(result);
  }

  private long estimateArray(Object obj) {
    long result = 3 * ref;
    int length = Array.getLength(obj);
    if (length != 0) {
      Class arrayElementClazz = obj.getClass().getComponentType();
      if (arrayElementClazz.isPrimitive()) {
        result += length * primitiveSizes.get(arrayElementClazz);
      } else {
        for (int i = 0; i < length; i++) {
          result += ref + estimateObject(Array.get(obj, i));
        }
      }
    }
    return align(result);
  }

  private boolean isSkipObject(Object obj) {
    return obj == null || isSharedFlyweight(obj) || visited.containsKey(obj);
  }

  private long align(long result) {
    if ((result % 8) != 0) {
      result += 8 - (result % 8);
    }
    return result;
  }

  private static final Map<Object, Integer> primitiveSizes = new IdentityHashMap<Object, Integer>() {
    {
      put(boolean.class, 1);
      put(byte.class, 1);
      put(char.class, 2);
      put(short.class, 2);
      put(int.class, 4);
      put(float.class, 4);
      put(double.class, 8);
      put(long.class, 8);
    }
  };

  /**
   * Returns true if this is a well-known shared flyweight.
   * For example, interned Strings, Booleans and Number objects.
   * 
   * thanks to Dr. Heinz Kabutz 
   * see http://www.javaspecialists.co.za/archive/Issue142.html
   */
  private static boolean isSharedFlyweight(Object obj) {
    // optimization - all of our flyweights are Comparable
    if (obj instanceof Comparable) {
      if (obj instanceof Enum) {
        return true;
      } else if (obj instanceof String) {
        // this will not cause a memory leak since
        // unused interned Strings will be thrown away
        return (obj == ((String) obj).intern());
      } else if (obj instanceof Boolean) {
        return (obj == Boolean.TRUE || obj == Boolean.FALSE);
      } else if (obj instanceof Integer) {
        return (obj == Integer.valueOf((Integer) obj));
      } else if (obj instanceof Short) {
        return (obj == Short.valueOf((Short) obj));
      } else if (obj instanceof Byte) {
        return (obj == Byte.valueOf((Byte) obj));
      } else if (obj instanceof Long) {
        return (obj == Long.valueOf((Long) obj));
      } else if (obj instanceof Character) {
        return (obj == Character.valueOf((Character) obj));
      }
    }
    return false;
  }
}
