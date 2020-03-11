package com.baremaps.osm.store;

import com.baremaps.osm.osmpbf.FileBlockConsumer;
import com.baremaps.osm.osmpbf.HeaderBlock;
import com.baremaps.osm.osmpbf.PrimitiveBlock;
import com.baremaps.osm.store.Store.Entry;
import com.baremaps.osm.postgis.PostgisHeaderStore;
import com.baremaps.osm.postgis.PostgisNodeStore;
import com.baremaps.osm.postgis.PostgisRelationStore;
import com.baremaps.osm.postgis.PostgisWayStore;
import java.util.stream.Collectors;

public class StoreConsumer extends FileBlockConsumer {

  private final PostgisHeaderStore headerStore;
  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  public StoreConsumer(
      PostgisHeaderStore headerStore,
      PostgisNodeStore nodeStore,
      PostgisWayStore wayStore,
      PostgisRelationStore relationStore) {
    this.headerStore = headerStore;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try {
      headerStore.insert(headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      nodeStore.importAll(
          primitiveBlock.getDenseNodes().stream()
              .map(node -> new Entry<>(node.getInfo().getId(), node))
              .collect(Collectors.toList()));
      wayStore.importAll(
          primitiveBlock.getWays().stream()
              .map(way -> new Entry<>(way.getInfo().getId(), way))
              .collect(Collectors.toList()));
      relationStore.importAll(
          primitiveBlock.getRelations().stream()
              .map(relation -> new Entry<>(relation.getInfo().getId(), relation))
              .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}