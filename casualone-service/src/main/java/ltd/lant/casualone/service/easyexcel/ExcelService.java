package ltd.lant.casualone.service.easyexcel;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhanglei
 * @description ExcelService
 * @date 2024/11/19  17:12
 */
public interface ExcelService {

    /**
     * 导出Excel，默认
     *
     * @param list     导出的数据
     * @param tClass   带有excel注解的实体类
     * @param response 相应
     * @return T
     * @author trg
     * @date 2024/1/15  17:32
     */
    <T> void exportExcel(List<T> list, Class<T> tClass, HttpServletResponse response) throws IOException;

    /**
     * 导出Excel，增加类型转换
     *
     * @param list     导出的数据
     * @param tClass   带有excel注解的实体类
     * @param response 相应
     * @return T
     * @author trg
     * @date 2024/1/15  17:32
     */
    <T, R> void exportExcel(List<T> list, Function<T, R> map, Class<R> tClass, HttpServletResponse response) throws IOException;


    /**
     * 导出Excel，按照模板导出，这里是填充模板
     *
     * @param list     导出的数据
     * @param tClass   带有excel注解的实体类
     * @param template 模板
     * @param response 相应
     * @return T
     * @author trg
     * @date 2024/1/15  17:32
     */
    <T> void exportExcel(List<T> list, Class<T> tClass, String template, HttpServletResponse response) throws IOException;

    /**
     * 导入Excel
     *
     * @param file          文件
     * @param tClass        带有excel注解的实体类
     * @param headRowNumber 表格头行数据
     * @param map           类型转换
     * @param consumer      消费数据的操作
     * @return T
     * @author trg
     * @date 2024/1/15  17:32
     */
    <T, R> void importExcel(MultipartFile file, Class<T> tClass, Integer headRowNumber, Function<T, R> map, Consumer<List<R>> consumer);


    /**
     * 导入Excel
     *
     * @param file          文件
     * @param tClass        带有excel注解的实体类
     * @param headRowNumber 表格头行数据
     * @param consumer      消费数据的操作
     * @return T
     * @author trg
     * @date 2024/1/15  17:32
     */
    <T> void importExcel(MultipartFile file, Class<T> tClass, Integer headRowNumber, Consumer<List<T>> consumer);

}
