package ltd.lant.casualone.service.easyexcel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Date;

/**
 * @author zhanglei
 * @description 日期格式转换工具
 * @date 2024/11/19  17:36
 */
public class DateConverter implements Converter<Date> {


    /*  DateConverter 使用示例
    @ExcelProperty(value = "创建时间",converter = DateConverter.class)
    private Date bornDate;*/
    
    @Override
    public Date convertToJavaData(ReadConverterContext<?> context) throws Exception {
        Class<?> aClass = context.getContentProperty().getField().getType();
        CellDataTypeEnum type = context.getReadCellData().getType();
        String stringValue = context.getReadCellData().getStringValue();
        if (aClass.equals(Date.class) && type.equals(CellDataTypeEnum.STRING) && ObjectUtils.isEmpty(stringValue)) {
            return null;
        }
        return Converter.super.convertToJavaData(context);
    }
}
