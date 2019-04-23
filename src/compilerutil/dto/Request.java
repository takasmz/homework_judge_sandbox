package compilerutil.dto;

/**
 * @Author 李如豪
 * @Date 2019/2/1 11:15
 * @VERSION 1.0
 **/
public class Request {
    private String command;
    private String data;
    private String examId;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
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
