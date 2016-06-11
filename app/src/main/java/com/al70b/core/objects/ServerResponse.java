package com.al70b.core.objects;

/**
 * Created by Naseem on 5/10/2015.
 */
public class ServerResponse<T> {

    private boolean success;
    private String errorMsg;
    private T result;

    public ServerResponse(boolean success, String errorMsg, T result) {
        this.success = success;

        if (!success)
            this.errorMsg = errorMsg;
        else
            this.result = result;
    }


    // if Server Response was positive an a result does exist
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return returns error message if server response was failure,
     * otherwise returns null
     */
    public String getErrorMsg() {
        if (!success)
            return errorMsg;

        return null;
    }


    /**
     * @return returns object of T when server response is success,
     * otherwise returns null
     */
    public T getResult() {
        if (success)
            return result;

        return null;
    }

}
