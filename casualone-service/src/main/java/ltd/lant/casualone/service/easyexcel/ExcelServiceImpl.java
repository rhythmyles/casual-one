package ltd.lant.casualone.service.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhanglei
 * @description ExcelServiceImpl
 * @date 2024/11/19  17:22
 */
@Service
public class ExcelServiceImpl implements ExcelService {

    @Override
    public <T> void exportExcel(List<T> list, Class<T> tClass, HttpServletResponse response) throws IOException {
        setResponse(response);
        EasyExcel.write(response.getOutputStream())
                .head(tClass)
                .excelType(ExcelTypeEnum.XLSX)
                .registerConverter(new LocalDateTimeConverter())
                .sheet("工作簿1")
                .doWrite(list);
    }

    /*    Function<T, R> 使用示例
    @PostMapping("/importExcel")
    public void importExcel(@RequestParam MultipartFile file){
    Function<TestEntity, TestVo> map = new Function<TestEntity, TestVo>() {
        @Override
        public TestVo apply(TestEntity testEntities) {
            TestVo testVo = new TestVo();
            testVo.setNum(testEntities.getNum());
            testVo.setSex(testEntities.getSex());
            testVo.setBaseName(testEntities.getName());
            return testVo;
        }
    };
    excelService.importExcel(file, TestEntity.class,2,map,testService::saveBatchTest);
    }*/
    @Override
    public <T, R> void exportExcel(List<T> list, Function<T, R> map, Class<R> tClass, HttpServletResponse response) throws IOException {
        setResponse(response);
        List<R> result = list.stream().map(map::apply).collect(Collectors.toList());
        exportExcel(result, tClass, response);
    }

    @Override
    public <T> void exportExcel(List<T> list, Class<T> tClass, String template, HttpServletResponse response) throws IOException {
        setResponse(response);
        EasyExcel.write(response.getOutputStream())
                .withTemplate(template)
                .excelType(ExcelTypeEnum.XLS)
                .useDefaultStyle(false)
                .registerConverter(new LocalDateTimeConverter())
                .sheet(0)
                .doFill(list);
    }

    @Override
    public <T, R> void importExcel(MultipartFile file, Class<T> tClass, Integer headRowNumber, Function<T, R> map, Consumer<List<R>> consumer) {
        List<T> excelData = ExcelUtil.readExcelData(file, tClass, headRowNumber);
        List<R> result = excelData.stream().map(map::apply).collect(Collectors.toList());
        consumer.accept(result);
    }

    @Override
    public <T> void importExcel(MultipartFile file, Class<T> tClass, Integer headRowNumber, Consumer<List<T>> consumer) {
        List<T> excelData = ExcelUtil.readExcelData(file, tClass, headRowNumber);
        consumer.accept(excelData);
    }

    public void setResponse(HttpServletResponse response) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("data", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xls");
    }
}
