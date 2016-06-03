package jc.download.interfac;

public interface ExceptionCode {

    int ERR_URL             = 0; // Illegal url.
    int ERR_CONN_IO         = 1; // IO exception while connecting.
    int ERR_CONN_RESPONSE   = 2; // Wrong response code while connecting.
    int ERR_CONN_LEN        = 3; // Invalid content length while connecting.
    int ERR_CONN_CANCEL     = 4; // Canceled while connecting.
    int ERR_FETCH_URL       = 5; // Illegal url while fetching.
    int ERR_FETCH_IO        = 6; // IO error while connecting when fetching.
    int ERR_FETCH_RESPONSE  = 7; // Unknown response code while fetching.
    int ERR_FETCH_DATA_IO   = 8; // IO error while getting input stream.
    int ERR_FETCH_FILE_IO   = 9; // IO error while getting random access file.
    int ERR_FETCH_CANCEL    = 10; // CANCEL
    int ERR_FETCH_PAUSE     = 11; // PAUSE
    int ERR_FETCH_SHELVE    = 12; // SHELVE
    int ERR_FETCH_READ      = 13; // IO error while reading data.
    int ERR_FETCH_WRITE     = 14; // IO error while writing to cache.
    int ERR_AT_VERIFY       = 15; // Error while verifying the file after complete.
}
