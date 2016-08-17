package sk.jmurin.android.testy.utils;

import java.util.List;

import sk.jmurin.android.testy.entities.TestInformation;

/**
 * Created by jan.murin on 16-Aug-16.
 */
public final class DownloadEvents {
    public static class NewTestsToDownload {
        public final List<TestInformation> nove;

        public NewTestsToDownload(List<TestInformation> nove) {
            this.nove = nove;
        }
    }

    public static class NothingToDownload {
    }

    public static class NewTestsDownloaded {
    }

//    public static class Progress {
//
//        public final int progress;
//
//        public Progress(int progress) {
//            this.progress = progress;
//        }
//    }
//
//    public static class Sucess {
//
//        public final String filePath;
//
//        public Sucess(String filePath) {
//            this.filePath = filePath;
//        }
//    }
//
//    public static class Fail {
//
//        public final String reason;
//
//        public Fail(String reason) {
//            this.reason = reason;
//        }
//    }
}
