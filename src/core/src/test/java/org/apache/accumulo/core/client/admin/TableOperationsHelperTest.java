/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.core.client.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.iterators.IteratorUtil.IteratorScope;
import org.apache.accumulo.core.iterators.conf.PerColumnIteratorConfig;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.util.BulkImportHelper.AssignmentStats;
import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

public class TableOperationsHelperTest {
  
  static class Tester extends TableOperationsHelper {
    Map<String,Map<String,String>> settings = new HashMap<String,Map<String,String>>();
    
    @Override
    public SortedSet<String> list() {
      return null;
    }
    
    @Override
    public boolean exists(String tableName) {
      return false;
    }
    
    @Override
    public void create(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {}
    
    @Override
    public void create(String tableName, boolean limitVersion) throws AccumuloException, AccumuloSecurityException, TableExistsException {
      create(tableName, limitVersion, TimeType.MILLIS);
    }
    
    @Override
    public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {}
    
    @Override
    public void addSplits(String tableName, SortedSet<Text> partitionKeys) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {}
    
    @Override
    public Collection<Text> getSplits(String tableName) throws TableNotFoundException {
      return null;
    }
    
    @Override
    public Collection<Text> getSplits(String tableName, int maxSplits) throws TableNotFoundException {
      return null;
    }
    
    @Override
    public Text getMaxRow(String tableName, Authorizations auths, Text startRow, boolean startInclusive, Text endRow, boolean endInclusive)
        throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
      return null;
    }
    
    @Override
    public void merge(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
      
    }
    
    @Override
    public void deleteRows(String tableName, Text start, Text end) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {}
    
    @Override
    public void compact(String tableName, Text start, Text end, boolean flush, boolean wait) throws AccumuloSecurityException, TableNotFoundException,
        AccumuloException {}
    
    @Override
    public void delete(String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {}
    
    @Override
    public void clone(String srcTableName, String newTableName, boolean flush, Map<String,String> propertiesToSet, Set<String> propertiesToExclude)
        throws AccumuloException, AccumuloSecurityException, TableNotFoundException, TableExistsException {}
    
    @Override
    public void rename(String oldTableName, String newTableName) throws AccumuloSecurityException, TableNotFoundException, AccumuloException,
        TableExistsException {}
    
    @Override
    public void flush(String tableName) throws AccumuloException, AccumuloSecurityException {}
    
    @Override
    public void flush(String tableName, Text start, Text end, boolean wait) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {}
    
    @Override
    public void setProperty(String tableName, String property, String value) throws AccumuloException, AccumuloSecurityException {
      if (!settings.containsKey(tableName))
        settings.put(tableName, new TreeMap<String,String>());
      settings.get(tableName).put(property, value);
    }
    
    @Override
    public void removeProperty(String tableName, String property) throws AccumuloException, AccumuloSecurityException {
      if (!settings.containsKey(tableName))
        return;
      settings.get(tableName).remove(property);
    }
    
    @Override
    public Iterable<Entry<String,String>> getProperties(String tableName) throws AccumuloException, TableNotFoundException {
      Map<String,String> empty = Collections.emptyMap();
      if (!settings.containsKey(tableName))
        return empty.entrySet();
      return settings.get(tableName).entrySet();
    }
    
    @Override
    public void setLocalityGroups(String tableName, Map<String,Set<Text>> groups) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {}
    
    @Override
    public Map<String,Set<Text>> getLocalityGroups(String tableName) throws AccumuloException, TableNotFoundException {
      return null;
    }
    
    @Override
    public Set<Range> splitRangeByTablets(String tableName, Range range, int maxSplits) throws AccumuloException, AccumuloSecurityException,
        TableNotFoundException {
      return null;
    }
    
    @Override
    public void importDirectory(String tableName, String dir, String failureDir, boolean setTime) throws TableNotFoundException, IOException,
        AccumuloException, AccumuloSecurityException {}
    
    @Override
    public AssignmentStats importDirectory(String tableName, String dir, String failureDir, int numThreads, int numAssignThreads, boolean disableGC)
        throws IOException, AccumuloException, AccumuloSecurityException {
      return null;
    }
    
    @Override
    public void offline(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
      
    }
    
    @Override
    public void online(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {}
    
    @Override
    public void clearLocatorCache(String tableName) throws TableNotFoundException {}
    
    @Override
    public Map<String,String> tableIdMap() {
      return null;
    }
    
    void check(String tablename, String[] values) {
      Map<String,String> expected = new TreeMap<String,String>();
      for (String value : values) {
        String parts[] = value.split("=", 2);
        expected.put(parts[0], parts[1]);
      }
      Assert.assertEquals(expected, settings.get(tablename));
    }
    
    /**
     * @deprecated since 1.4 {@link #attachIterator(String, IteratorSetting)}
     */
    @Override
    public void addAggregators(String tableName, List<? extends PerColumnIteratorConfig> aggregators) throws AccumuloSecurityException, TableNotFoundException,
        AccumuloException {}
  }
  
  @Test
  public void testAttachIterator() throws Exception {
    Tester t = new Tester();
    Map<String,String> empty = Collections.emptyMap();
    t.attachIterator("table", new IteratorSetting(10, "someName", "foo.bar", EnumSet.of(IteratorScope.scan), empty));
    t.check("table", new String[] {"table.iterator.scan.someName=10,foo.bar",});
    t.removeIterator("table", "someName", EnumSet.of(IteratorScope.scan));
    t.check("table", new String[] {});
    
    IteratorSetting setting = new IteratorSetting(10, "someName", "foo.bar");
    setting.setScopes(EnumSet.of(IteratorScope.majc));
    setting.addOptions(Collections.singletonMap("key", "value"));
    t.attachIterator("table", setting);
    setting = new IteratorSetting(10, "someName", "foo.bar");
    t.attachIterator("table", setting);
    t.check("table", new String[] {"table.iterator.majc.someName=10,foo.bar", "table.iterator.majc.someName.opt.key=value",
        "table.iterator.scan.someName=10,foo.bar",});
    
    setting = new IteratorSetting(20, "otherName", "some.classname");
    setting.setScopes(EnumSet.of(IteratorScope.majc));
    setting.addOptions(Collections.singletonMap("key", "value"));
    t.attachIterator("table", setting);
    setting = new IteratorSetting(20, "otherName", "some.classname");
    t.attachIterator("table", setting);
    Set<String> two = t.listIterators("table");
    Assert.assertEquals(2, two.size());
    Assert.assertTrue(two.contains("otherName"));
    Assert.assertTrue(two.contains("someName"));
    t.removeIterator("table", "someName", EnumSet.allOf(IteratorScope.class));
    t.check("table", new String[] {"table.iterator.majc.otherName=20,some.classname", "table.iterator.majc.otherName.opt.key=value",
        "table.iterator.scan.otherName=20,some.classname",});
    
    setting = t.getIteratorSetting("table", "otherName", IteratorScope.scan);
    Assert.assertEquals(20, setting.getPriority());
    Assert.assertEquals("some.classname", setting.getIteratorClass());
    Assert.assertFalse(setting.hasProperties());
    setting = t.getIteratorSetting("table", "otherName", IteratorScope.majc);
    Assert.assertEquals(20, setting.getPriority());
    Assert.assertEquals("some.classname", setting.getIteratorClass());
    Assert.assertTrue(setting.hasProperties());
    Assert.assertEquals(Collections.singletonMap("key", "value"), setting.getProperties());
    setting.setScopes(EnumSet.of(IteratorScope.minc));
    t.attachIterator("table", setting);
    t.check("table", new String[] {"table.iterator.majc.otherName=20,some.classname", "table.iterator.majc.otherName.opt.key=value",
        "table.iterator.minc.otherName=20,some.classname", "table.iterator.minc.otherName.opt.key=value", "table.iterator.scan.otherName=20,some.classname",});
    
    try {
      t.attachIterator("table", setting);
      Assert.fail();
    } catch (IllegalArgumentException e) {}
    setting.setName("thirdName");
    try {
      t.attachIterator("table", setting);
      Assert.fail();
    } catch (IllegalArgumentException e) {}
    setting.setPriority(10);
    t.setProperty("table", "table.iterator.minc.thirdName.opt.key", "value");
    try {
      t.attachIterator("table", setting);
      Assert.fail();
    } catch (IllegalArgumentException e) {}
    t.removeProperty("table", "table.iterator.minc.thirdName.opt.key");
    t.attachIterator("table", setting);
  }
}
