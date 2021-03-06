/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.accumulo.core.client.impl.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum TableOperationExceptionType implements org.apache.thrift.TEnum {
  EXISTS(0),
  NOTFOUND(1),
  OFFLINE(2),
  BULK_BAD_INPUT_DIRECTORY(3),
  BULK_BAD_ERROR_DIRECTORY(4),
  BAD_RANGE(5),
  OTHER(6);

  private final int value;

  private TableOperationExceptionType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static TableOperationExceptionType findByValue(int value) { 
    switch (value) {
      case 0:
        return EXISTS;
      case 1:
        return NOTFOUND;
      case 2:
        return OFFLINE;
      case 3:
        return BULK_BAD_INPUT_DIRECTORY;
      case 4:
        return BULK_BAD_ERROR_DIRECTORY;
      case 5:
        return BAD_RANGE;
      case 6:
        return OTHER;
      default:
        return null;
    }
  }
}
