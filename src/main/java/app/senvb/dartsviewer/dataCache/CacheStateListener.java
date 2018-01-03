package app.senvb.dartsviewer.dataCache;

import java.io.IOException;

public interface CacheStateListener {

    int clearCache() throws IOException;

    double calculateTotalCacheSizeInKB();
}
