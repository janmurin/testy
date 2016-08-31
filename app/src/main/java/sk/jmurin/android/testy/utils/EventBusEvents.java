package sk.jmurin.android.testy.utils;

import java.util.List;

import sk.jmurin.android.testy.entities.SkoreStats;


/**
 * Created by jan.murin on 16-Aug-16.
 */
public final class EventBusEvents {
    public static class NewTestsToDownload {

        private final List<Integer> nove;

        public NewTestsToDownload(List<Integer> nove) {
            this.nove = nove;
        }
    }

    public static class NothingToDownload {
    }

    public static class NewTestsDownloaded {
    }

    public static class DalejButtonClicked {
    }

    public static class UsernameSelected {
        public final String meno;

        public UsernameSelected(String meno) {
            this.meno=meno;
        }
    }

    public static class ZavrietTutorial {
    }

    public static class SkoreStatsDownloaded {
        public final List<SkoreStats> skores;

        public SkoreStatsDownloaded(List<SkoreStats> skores) {
            this.skores=skores;
        }
    }

    public static class DownloadError {
        public final String s;

        public DownloadError(String s) {
            this.s=s;
        }
    }

    public static class InsertStatsResponse {
        public final String messsage;

        public InsertStatsResponse(String s) {
            this.messsage=s;
        }
    }
}
