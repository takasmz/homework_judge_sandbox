package compilerutil.dto;

/**
 * @Author 李如豪
 * @Date 2019/2/13 17:21
 * @VERSION 1.0
 **/
public class ExamTestCase {
    private Integer id;

    /**
     * 题目id
     */
    private Integer examId;

    /**
     * 一次输入
     */
    private String input;

    /**
     * 一次输出
     */
    private String output;

    private static final long serialVersionUID = 1L;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取题目id
     *
     * @return exam_id - 题目id
     */
    public Integer getExamId() {
        return examId;
    }

    /**
     * 设置题目id
     *
     * @param examId 题目id
     */
    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    /**
     * 获取一次输入
     *
     * @return input - 一次输入
     */
    public String getInput() {
        return input;
    }

    /**
     * 设置一次输入
     *
     * @param input 一次输入
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * 获取一次输出
     *
     * @return output - 一次输出
     */
    public String getOutput() {
        return output;
    }

    /**
     * 设置一次输出
     *
     * @param output 一次输出
     */
    public void setOutput(String output) {
        this.output = output;
    }
}
