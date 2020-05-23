package open.dolphin.delegater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;

/**
 * BusinessDelegater.
 *
 * @author pns
 */
public class BusinessDelegater<T> {
    protected Logger logger;
    private Result errorCode = Result.NO_ERROR;
    private String errorMessage = "delegate error";
    // 呼び出し元の class
    private Class<T> target;

    public BusinessDelegater() {
        // 呼び出し元の class を取得
        String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        try {
            target = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            processError(e);
        }
        logger = LoggerFactory.getLogger(target);
    }

    public T getService() {
        try {
            return DolphinClientContext.getContext().getWebTarget().proxy(target);
        } catch (Exception e) {
            processError(e);
        }
        return null;
    }

    public Result getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Result errCode) {
        this.errorCode = errCode;
    }

    public boolean isNoError() {
        return errorCode.equals(Result.NO_ERROR);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMesseage) {
        this.errorMessage = errorMesseage;
    }

    public void processError(Exception e) {
        setErrorMessage(e.toString());
        setErrorCode(Result.ERROR);
        e.printStackTrace(System.err);
    }

    public enum Result {NO_ERROR, ERROR}
}
