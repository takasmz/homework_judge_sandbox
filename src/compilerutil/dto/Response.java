package compilerutil.dto;

/**
 * @Author 李如豪
 * @Date 2019/2/1 10:43
 * @VERSION 1.0
 **/
public class Response {
    private String requestCommand;
    private String responseCommand;
    private String data;
    private String examId;

    public String getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(String responseCommand) {
        this.responseCommand = responseCommand;
    }

    public String getRequestCommand() {
        return requestCommand;
    }

    public void setRequestCommand(String requestCommand) {
        this.requestCommand = requestCommand;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String signalId) {
        this.examId = signalId;
    }
}
