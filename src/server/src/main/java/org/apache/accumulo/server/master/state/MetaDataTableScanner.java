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
/**
 * 
 */
package org.apache.accumulo.server.master.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.KeyExtent;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.WholeRowIterator;
import org.apache.accumulo.core.util.ColumnFQ;
import org.apache.accumulo.server.client.HdfsZooInstance;
import org.apache.accumulo.server.security.SecurityConstants;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

public class MetaDataTableScanner implements Iterator<TabletLocationState> {
  private static final Logger log = Logger.getLogger(MetaDataTableScanner.class);
  
  BatchScanner mdScanner;
  Iterator<Entry<Key,Value>> iter;
  
  public MetaDataTableScanner(Range range, CurrentState state) {
    // scan over metadata table, looking for tablets in the wrong state based on the live servers and online tables
    try {
      Connector connector = HdfsZooInstance.getInstance().getConnector(SecurityConstants.getSystemCredentials());
      mdScanner = connector.createBatchScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS, 8);
      ColumnFQ.fetch(mdScanner, Constants.METADATA_PREV_ROW_COLUMN);
      mdScanner.fetchColumnFamily(Constants.METADATA_CURRENT_LOCATION_COLUMN_FAMILY);
      mdScanner.fetchColumnFamily(Constants.METADATA_FUTURE_LOCATION_COLUMN_FAMILY);
      mdScanner.fetchColumnFamily(Constants.METADATA_LOG_COLUMN_FAMILY);
      mdScanner.fetchColumnFamily(Constants.METADATA_CHOPPED_COLUMN_FAMILY);
      mdScanner.setRanges(Collections.singletonList(range));
      mdScanner.addScanIterator(new IteratorSetting(1000, "wholeRows", WholeRowIterator.class));
      IteratorSetting tabletChange = new IteratorSetting(1001, "tabletChange", TabletStateChangeIterator.class);
      if (state != null) {
        TabletStateChangeIterator.setCurrentServers(tabletChange, state.onlineTabletServers());
        TabletStateChangeIterator.setOnlineTables(tabletChange, state.onlineTables());
        TabletStateChangeIterator.setMerges(tabletChange, state.merges());
      }
      mdScanner.addScanIterator(tabletChange);
      iter = mdScanner.iterator();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  public MetaDataTableScanner(Range range) {
    this(range, null);
  }
  
  public void close() {
    mdScanner.close();
    iter = null;
  }
  
  public void finalize() {
    close();
  }
  
  @Override
  public boolean hasNext() {
    boolean result = iter.hasNext();
    if (!result)
      mdScanner.close();
    return result;
  }
  
  @Override
  public TabletLocationState next() {
    return fetch();
  }
  
  public static TabletLocationState createTabletLocationState(SortedMap<Key,Value> decodedRow) {
    KeyExtent extent = null;
    TServerInstance future = null;
    TServerInstance current = null;
    TServerInstance last = null;
    List<Collection<String>> walogs = new ArrayList<Collection<String>>();
    boolean chopped = false;
    
    for (Entry<Key,Value> entry : decodedRow.entrySet()) {
      Key key = entry.getKey();
      Text row = key.getRow();
      Text cf = key.getColumnFamily();
      Text cq = key.getColumnQualifier();
      
      if (cf.compareTo(Constants.METADATA_FUTURE_LOCATION_COLUMN_FAMILY) == 0) {
        future = new TServerInstance(entry.getValue(), cq);
      } else if (cf.compareTo(Constants.METADATA_CURRENT_LOCATION_COLUMN_FAMILY) == 0) {
        current = new TServerInstance(entry.getValue(), cq);
      } else if (cf.compareTo(Constants.METADATA_LOG_COLUMN_FAMILY) == 0) {
        String[] split = entry.getValue().toString().split("\\|")[0].split(";");
        walogs.add(Arrays.asList(split));
      } else if (cf.compareTo(Constants.METADATA_LAST_LOCATION_COLUMN_FAMILY) == 0) {
        last = new TServerInstance(entry.getValue(), cq);
      } else if (cf.compareTo(Constants.METADATA_CHOPPED_COLUMN_FAMILY) == 0) {
        chopped = true;
      } else if (Constants.METADATA_PREV_ROW_COLUMN.equals(cf, cq)) {
        extent = new KeyExtent(row, entry.getValue());
      }
    }
    if (extent == null) {
      log.warn("No prev-row for key extent: " + decodedRow);
      return null;
    }
    return new TabletLocationState(extent, future, current, last, walogs, chopped);
  }
  
  private TabletLocationState fetch() {
    Entry<Key,Value> entry = iter.next();
    try {
      final SortedMap<Key,Value> decodedRow = WholeRowIterator.decodeRow(entry.getKey(), entry.getValue());
      return createTabletLocationState(decodedRow);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public void remove() {
    throw new RuntimeException("Unimplemented");
  }
}
