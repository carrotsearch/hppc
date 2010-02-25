
SELECT 
 RPAD('Test name', 40) ||
 LPAD('[s]', 7)            ||
 LPAD('+/-', 7)            ||
 LPAD('GC#', 7)            ||
 LPAD('GC[s]', 7);


SELECT STRINGDECODE('## Object stacks:');

SELECT 
  RPAD(REPLACE(CLASSNAME,'com.carrotsearch.hppc.') || '.' || NAME, 40) ||
  LPAD(ROUND(ROUND_AVG, 3), 7)                 ||
  LPAD(ROUND(ROUND_STDDEV, 3), 7)              ||
  LPAD(GC_INVOCATIONS, 7)            ||
  LPAD(ROUND(GC_TIME, 3), 7)
FROM TESTS, RUNS R
WHERE RUN_ID = (SELECT MAX(ID) FROM RUNS) 
  AND RUN_ID = R.ID 
  AND (CLASSNAME LIKE '%ObjectStack%'  OR CLASSNAME LIKE '%ArrayDeque%')
ORDER BY CLASSNAME ASC, NAME ASC; 


SELECT STRINGDECODE('## Primitive stacks:');

SELECT 
  RPAD(REPLACE(CLASSNAME,'com.carrotsearch.hppc.') || '.' || NAME, 40) ||
  LPAD(ROUND(ROUND_AVG, 3), 7)                 ||
  LPAD(ROUND(ROUND_STDDEV, 3), 7)              ||
  LPAD(GC_INVOCATIONS, 7)            ||
  LPAD(ROUND(GC_TIME, 3), 7)
FROM TESTS, RUNS R
WHERE RUN_ID = (SELECT MAX(ID) FROM RUNS) 
  AND RUN_ID = R.ID 
  AND (CLASSNAME LIKE '%StackBenchmark%' AND CLASSNAME NOT LIKE '%Object%')
ORDER BY CLASSNAME ASC, NAME ASC;


SELECT STRINGDECODE('## Iteration strategies:');

SELECT 
  RPAD(REPLACE(CLASSNAME,'com.carrotsearch.hppc.') || '.' || NAME, 50) ||
  LPAD(ROUND(ROUND_AVG, 3), 7)                 ||
  LPAD(ROUND(ROUND_STDDEV, 3), 7)              ||
  LPAD(GC_INVOCATIONS, 7)            ||
  LPAD(ROUND(GC_TIME, 3), 7)
FROM TESTS, RUNS R
WHERE RUN_ID = (SELECT MAX(ID) FROM RUNS) 
  AND RUN_ID = R.ID 
  AND (REPLACE(CLASSNAME,'com.carrotsearch.hppc.') 
      IN ('ByteArrayListBenchmark', 'LongArrayListBenchmark', 'ObjectArrayListBenchmark'))
ORDER BY CLASSNAME ASC, NAME ASC;


SELECT STRINGDECODE('## Bit set:');

SELECT 
  RPAD(REPLACE(CLASSNAME,'com.carrotsearch.hppc.') || '.' || NAME, 50) ||
  LPAD(ROUND(ROUND_AVG, 3), 7)                 ||
  LPAD(ROUND(ROUND_STDDEV, 3), 7)              ||
  LPAD(GC_INVOCATIONS, 7)            ||
  LPAD(ROUND(GC_TIME, 3), 7)
FROM TESTS, RUNS R
WHERE RUN_ID = (SELECT MAX(ID) FROM RUNS) 
  AND RUN_ID = R.ID 
  AND (CLASSNAME LIKE ('%BitSetBenchmark'))
ORDER BY CLASSNAME ASC, NAME ASC;

