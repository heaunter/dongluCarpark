package com.donglu.carpark.ui.view.setting.wizard;

import java.util.ArrayList;
import java.util.List;

import com.dongluhitec.card.domain.db.DomainObject;
import com.dongluhitec.card.domain.db.singlecarpark.CameraTypeEnum;
import com.dongluhitec.card.hardware.plateDevice.bean.PlateDownload;

public class DownloadPlateModel extends DomainObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<DownloadDeviceInfo> list=new ArrayList<>();
	private List<DownloadDeviceInfo> listSelected=new ArrayList<>();
	private DownloadDeviceInfo info;
	private CameraTypeEnum type=CameraTypeEnum.信路威;
	private String ip="192.168.1.233";
	
	private List<PlateDownload> listPlate=new ArrayList<>();
	
	public List<DownloadDeviceInfo> getList() {
		return list;
	}
	public void setList(List<DownloadDeviceInfo> list) {
		this.list = list;
		if (pcs != null)
			pcs.firePropertyChange("list", null, null);
	}
	public DownloadDeviceInfo getInfo() {
		return info;
	}
	public void setInfo(DownloadDeviceInfo info) {
		this.info = info;
		if (pcs != null)
			pcs.firePropertyChange("info", null, null);
	}
	public CameraTypeEnum getType() {
		return type;
	}
	public void setType(CameraTypeEnum type) {
		this.type = type;
		if (pcs != null)
			pcs.firePropertyChange("type", null, null);
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
		if (pcs != null)
			pcs.firePropertyChange("ip", null, null);
	}
	public void addInfo(DownloadDeviceInfo d) {
		if (!list.contains(d)) {
			list.add(d);
			if (pcs != null)
				pcs.firePropertyChange("list", null, null);
			if (!listSelected.contains(d)) {
				listSelected.add(d);
				if (pcs != null)
					pcs.firePropertyChange("listSelected", null, null);
			}
		}
		
	}
	public List<DownloadDeviceInfo> getListSelected() {
		return listSelected;
	}
	public void setListSelected(List<DownloadDeviceInfo> listSelected) {
		this.listSelected = listSelected;
		if (pcs != null)
			pcs.firePropertyChange("listSelected", null, null);
	}
	public void removeInfo(java.util.List<DownloadDeviceInfo> listSelected2) {
		for (DownloadDeviceInfo downloadDeviceInfo : listSelected2) {
			list.remove(downloadDeviceInfo);
		}
		if (pcs != null)
			pcs.firePropertyChange("list", null, null);
	}
	public List<PlateDownload> getListPlate() {
		return listPlate;
	}
	public void setListPlate(List<PlateDownload> listPlate) {
		this.listPlate = listPlate;
		if (pcs != null)
			pcs.firePropertyChange("listPlate", null, null);
	}
	
}
