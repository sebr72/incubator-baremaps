package io.gazetteer.tileserver;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.Coordinate;
import io.gazetteer.mbtiles.MBTiles;
import io.gazetteer.mbtiles.Metadata;
import io.gazetteer.mbtiles.Tile;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class MBTilesDataSource implements TileDataSource {

    public final SQLiteDataSource dataSource;

    public final Metadata metadata;

    public final int cacheSize;

    private final AsyncLoadingCache<Coordinate, Tile> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
            .buildAsync(coord -> loadTile(coord));

    public MBTilesDataSource(SQLiteDataSource dataSource, Metadata metadata) {
        this(dataSource, metadata, 10000);
    }

    public MBTilesDataSource(SQLiteDataSource dataSource, Metadata metadata, int cacheSize) {
        this.dataSource = dataSource;
        this.metadata = metadata;
        this.cacheSize = cacheSize;
    }

    public String getMimeType() {
        return metadata.format.mimeType;
    }

    private Tile loadTile(Coordinate coordinate) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return MBTiles.getTile(connection, coordinate);
        }
    }

    @Override
    public CompletableFuture<Tile> getTile(Coordinate coordinate) {
        return cache.get(coordinate);
    }

    public static MBTilesDataSource fromDataSource(SQLiteDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> map = MBTiles.getMetadata(connection);
            Metadata metadata = Metadata.fromMap(map);
            return new MBTilesDataSource(dataSource, metadata);
        }
    }


}
