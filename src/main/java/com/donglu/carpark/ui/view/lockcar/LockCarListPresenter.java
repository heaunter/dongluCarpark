package com.donglu.carpark.ui.view.lockcar;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.donglu.carpark.service.CarparkDatabaseServiceProvider;
import com.donglu.carpark.service.CarparkService;
import com.donglu.carpark.service.CarparkUserService;
import com.donglu.carpark.ui.common.AbstractListPresenter;
import com.donglu.carpark.ui.view.lockcar.wizard.LocaCarWizard;
import com.donglu.carpark.ui.wizard.AddUserModel;
import com.donglu.carpark.ui.wizard.AddUserWizard;
import com.donglu.carpark.ui.wizard.monthcharge.MonthlyUserPayModel;
import com.donglu.carpark.ui.wizard.monthcharge.MonthlyUserPayWizard;
import com.donglu.carpark.util.ExcelImportExport;
import com.donglu.carpark.util.ExcelImportExportImpl;
import com.dongluhitec.card.common.ui.CommonUIFacility;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkCarpark;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkLockCar;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkMonthlyCharge;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkUser;
import com.dongluhitec.card.domain.db.singlecarpark.SystemOperaLogTypeEnum;
import com.dongluhitec.card.domain.util.StrUtil;
import com.google.inject.Inject;

public class LockCarListPresenter extends AbstractListPresenter<SingleCarparkLockCar>{
	LockCarListView view;
	
	String plateNo;
	String status;
	String operaName; 
	Date start;
	Date end;
	
	@Inject
	private CommonUIFacility commonui;
	@Inject
	private CarparkDatabaseServiceProvider sp;
	@Override
	public void go(Composite c) {
		view=new LockCarListView(c,c.getStyle());
		view.setPresenter(this);
		view.setTableTitle("固定用户列表");
		view.setShowMoreBtn(false);
		refresh();
	}

	
	public void add() {}

	@Override
	public void refresh() {
		List<SingleCarparkLockCar> findByNameOrPlateNo = sp.getCarparkInOutService().findLockCar(plateNo,status,operaName,new Date(),new Date());
		view.getModel().setList(findByNameOrPlateNo);
		view.getModel().setCountSearch(findByNameOrPlateNo.size());
		view.getModel().setCountSearchAll(findByNameOrPlateNo.size());
	}

	public void search(String plateNo, String operaName, String status, Date start, Date end) {
		this.operaName=operaName;
		this.plateNo=plateNo;
		this.status=status.equals("全部")?null:status;
		this.start=start;
		this.end=end;
		refresh();
	}

	public void lockCar() {
		LocaCarWizard w=new LocaCarWizard(new SingleCarparkLockCar());
		
		SingleCarparkLockCar m = (SingleCarparkLockCar) commonui.showWizard(w);
		if (StrUtil.isEmpty(w)) {
			return;
		}
		m.setStatus("已锁定");
		m.setOperaName(System.getProperty("userName"));
		m.setCreateTime(new Date());
		sp.getCarparkInOutService().saveLockCar(m);
		sp.getCarparkInOutService().lockCar(m.getPlateNO());
	}


	public void unlockCar() {
		List<SingleCarparkLockCar> selected = view.getModel().getSelected();
		if (StrUtil.isEmpty(selected)) {
			return;
		}
		for (SingleCarparkLockCar m : selected) {
			m=sp.getCarparkInOutService().findLockCarByPlateNO(m.getPlateNO(),null);
			if (m.getStatus().equals(SingleCarparkLockCar.Status.已解锁)) {
				continue;
			}
			
			m.setStatus(SingleCarparkLockCar.Status.已解锁.name());
			sp.getCarparkInOutService().saveLockCar(m);
		}
		refresh();
	}
	
}
