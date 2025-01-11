package scraper.plugins.core.debugger.Utils;

public class StoppedException extends Exception{
    private static final System.Logger l = System.getLogger("Debugger");

    public StoppedException(String errorMessage){
        super(errorMessage);
        l.log(System.Logger.Level.ERROR, errorMessage);
    }

    public StoppedException(String errorMessage, Throwable err) {
        super(errorMessage, err);
        l.log(System.Logger.Level.ERROR, errorMessage);
    }
}
