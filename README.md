Fragment Reader for Jackson
============================

A `Reader` based on String fragments and parameters for Jackson intended to be used with String templates.

Advantages
----------

- Avoids large intermediate allocations
- Uses `ObjectMapper` to correctly serialize values instead of relying on their string representation.



Limitations
-----------

- String parameters are currently always quoted breaking interpolation for String literals

```java
"""
{
  "key": "the value is \{value}"
}
"""
