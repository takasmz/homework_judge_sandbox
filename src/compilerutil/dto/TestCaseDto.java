package compilerutil.dto;


import java.util.List;

/**
 * @Author 李如豪
 * @Date 2019/2/1 13:43
 * @VERSION 1.0
 **/
public class TestCaseDto {
    private String code;
    private List<ExamTestCase> testCaseItem;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<ExamTestCase> getTestCaseItem() {
        return testCaseItem;
    }

    public void setTestCaseItem(List<ExamTestCase> testCaseItem) {
        this.testCaseItem = testCaseItem;
    }
}
