package compilerutil;


import compilerutil.dto.ExamResult;
import compilerutil.dto.ExamResultDto;
import compilerutil.dto.ExamTestCase;
import compilerutil.dto.TestCaseDto;
import compilerutil.stream.CacheOutputStream;
import compilerutil.stream.ThreadInputStream;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @Author 李如豪
 * @Date 2019/2/1 11:45
 * @VERSION 1.0
 **/
public class ExamCallable implements Callable<ExamResult> {
    private ThreadInputStream systemThreadIn;
    private TestCaseDto testCaseDto;
    private CacheOutputStream resultBuffer;

    public ExamCallable(ThreadInputStream systemThreadIn,TestCaseDto testCaseDto,CacheOutputStream resultBuffer){
        this.resultBuffer = resultBuffer;
        this.systemThreadIn = systemThreadIn;
        this.testCaseDto = testCaseDto;
    }

    @Override
    public ExamResult call() throws Exception {
        DynamicEngine de = DynamicEngine.getInstance();
        ExamResultDto results = (ExamResultDto) de.javaCodeToObject("Main",testCaseDto.getCode());
        ExamResult examResult = new ExamResult();

        if(results.getInstance() != null){
            Object instance = results.getInstance();
            Method method = results.getMethod();
            if (!Modifier.isStatic(method.getModifiers()))
                throw new Exception("main方法不是静态方法");
            method.setAccessible(true);
            List<ExamTestCase> testCaseList = testCaseDto.getTestCaseItem();
            examResult.setTotalTestcases(testCaseList.size());
            examResult.setTotalCorrect(testCaseList.size());
            for (int i = 0;i<testCaseList.size(); i++) {
                try {
                    ExamTestCase testCase = testCaseList.get(i);
                    String inputParams = testCase.getInput();
                    systemThreadIn.setThreadIn(new ByteArrayInputStream(inputParams.getBytes()));
                    System.setIn(systemThreadIn);
                    System.setOut(new PrintStream(resultBuffer));
                    method.invoke(instance, (Object) new String[]{});//方式一
                    String result = new String(resultBuffer.removeBytes(Thread
                            .currentThread().getId())).replaceAll("\r\n","");
                    if (CheckAnswer(testCase.getOutput(),result)) {
                        examResult.setStatus("Wrong Answer");
                        examResult.setError(testCase.getInput() + "," +result + "," + testCase.getOutput());
                        examResult.setTotalCorrect(i);
                        break;
                    }
                    examResult.setStatus("Accepted");
                } catch (Exception e) {
                    e.printStackTrace();
                    examResult.setStatus("Compile Error");
                    examResult.setError(e.getCause().toString());
                    StringBuilder sb = new StringBuilder();
                    for (StackTraceElement element : e.getStackTrace()) {
                        sb.append("\r\n\t").append(element);
                    }
                    examResult.setError(sb.length() == 0 ? null : sb.toString());
                    examResult.setNormal(false);
                    break;
                }finally {
                    systemThreadIn.removeAndCloseThreadIn();
                }
            }
        }else{
            examResult.setStatus("Compile Error");
        }
        return examResult;
    }

    private boolean CheckAnswer(String answer,String result) {

        if(answer.contains(" ")) {
            long num = Arrays.stream(answer.split(" ")).filter(result::contains).count();
            return num == answer.split(" ").length;
        }else {
            return !result.equals(answer);
        }
    }
}
