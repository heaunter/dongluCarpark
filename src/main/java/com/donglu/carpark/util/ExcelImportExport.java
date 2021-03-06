package com.donglu.carpark.util;

import java.util.List;

import com.donglu.carpark.service.CarparkDatabaseServiceProvider;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkUser;
import com.dongluhitec.card.ui.util.impl.ExcelImportExportImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(ExcelImportExportImpl.class)
public interface ExcelImportExport {
	
	/**
	 * 所有的excel模板都应该在这里进行定义
	 */
	String UserTemplate = System.getProperty("user.dir") + "/excelTemplete/固定用户导入导出模板.xls";
	String NomalTemplate = System.getProperty("user.dir") + "/excelTemplete/导出通用模板.xls";

    void printExcel(String filePath) throws Exception;
	/**
	 * 获取excel的行数
	 * @param excelPath
	 * @return
	 */
	int getExcelRowNum(String excelPath);
	
	public void exportUser(String path,List<SingleCarparkUser> list) throws Exception ;
	public int importUser(String path,CarparkDatabaseServiceProvider sp) throws Exception ;
	public void export(String path,String[] names,String[] cloumns,List<? extends Object> list)throws Exception;
}
