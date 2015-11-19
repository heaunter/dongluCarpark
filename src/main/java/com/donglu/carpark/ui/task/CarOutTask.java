package com.donglu.carpark.ui.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.donglu.carpark.model.CarparkMainModel;
import com.donglu.carpark.service.CarparkDatabaseServiceProvider;
import com.donglu.carpark.service.CarparkInOutServiceI;
import com.donglu.carpark.ui.CarparkMainApp;
import com.donglu.carpark.ui.CarparkMainPresenter;
import com.donglu.carpark.util.CarparkUtils;
import com.dongluhitec.card.domain.db.singlecarpark.CarTypeEnum;
import com.dongluhitec.card.domain.db.singlecarpark.DeviceRoadTypeEnum;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkCarpark;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkDevice;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkInOutHistory;
import com.dongluhitec.card.domain.db.singlecarpark.SingleCarparkUser;
import com.dongluhitec.card.domain.db.singlecarpark.SystemSettingTypeEnum;
import com.dongluhitec.card.domain.util.StrUtil;
import com.google.common.collect.Maps;

public class CarOutTask implements Runnable{
	private static Logger LOGGER = LoggerFactory.getLogger(CarInTask.class);
	
	private static Image outSmallImage;
	private static Image outBigImage;
	
	private final String plateNO;
	private final String ip;
	private final CarparkMainModel model;
	private final CarparkDatabaseServiceProvider sp;
	private final CarparkMainPresenter presenter;
	private final CLabel lbl_outBigImg;
	private final CLabel lbl_outSmallImg;
	private final CLabel lbl_inBigImg;
	private final CLabel lbl_inSmallImg;
	
	private final Text text_real;
	
	private final byte[] bigImage;
	private final byte[] smallImage;
	private final Shell shell;
	private final Combo carTypeSelectCombo;
	
	// 保存车牌最近的处理时间
	private final Map<String, Date> mapPlateNoDate = CarparkMainApp.mapPlateNoDate;
	// 保存设备的信息
	private final Map<String, SingleCarparkDevice> mapIpToDevice = CarparkMainApp.mapIpToDevice;
	// 保存设置信息
	private final Map<SystemSettingTypeEnum, String> mapSystemSetting = CarparkMainApp.mapSystemSetting;
	// 保存最近的手动拍照时间
	private final Map<String, Date> mapHandPhotograph = CarparkMainApp.mapHandPhotograph;
	private final Map<String, Boolean> mapOpenDoor = CarparkMainApp.mapOpenDoor;
	public static Map<String, String> mapTempCharge=CarparkMainApp.mapTempCharge;
	
	public CarOutTask(String ip, String plateNO, byte[] bigImage, byte[] smallImage,CarparkMainModel model,
			CarparkDatabaseServiceProvider sp, CarparkMainPresenter presenter, CLabel lbl_outBigImg,
			CLabel lbl_outSmallImg,CLabel lbl_inBigImg,CLabel lbl_inSmallImg,Combo carTypeSelectCombo,Text text_real,Shell shell) {
		super();
		this.ip = ip;
		this.plateNO = plateNO;
		this.bigImage = bigImage;
		this.smallImage = smallImage;
		this.model = model;
		this.sp = sp;
		this.presenter = presenter;
		this.lbl_outBigImg = lbl_outBigImg;
		this.lbl_outSmallImg = lbl_outSmallImg;
		this.lbl_inBigImg=lbl_inBigImg;
		this.lbl_inSmallImg=lbl_inSmallImg;
		this.carTypeSelectCombo=carTypeSelectCombo;
		this.text_real=text_real;
		this.shell = shell;
	}
	
	public void run(){

		Boolean boolean1 = mapOpenDoor.get(ip);
		if (boolean1 != null && boolean1) {
			mapOpenDoor.put(ip, null);
			presenter.saveOpenDoor(mapIpToDevice.get(ip), bigImage, plateNO, false);
			return;
		}
		model.setDisContinue(false);
		model.setHandSearch(false);
		long nanoTime = System.nanoTime();
		Date date = new Date();
		boolean checkPlateNODiscernGap = presenter.checkPlateNODiscernGap(mapPlateNoDate, plateNO, date);
		if (!checkPlateNODiscernGap) {
			return;
		}
		mapPlateNoDate.put(plateNO, date);
		//
		String folder = StrUtil.formatDate(date, "yyyy/MM/dd/HH");
		String fileName = StrUtil.formatDate(date, "yyyyMMddHHmmssSSS");
		String bigImgFileName = fileName + "_" + plateNO + "_big.jpg";
		presenter.saveImage(folder, bigImgFileName, bigImage);
		String smallImgFileName = fileName + "_" + plateNO + "_small.jpg";
		presenter.saveImage(folder, smallImgFileName, smallImage);
		long nanoTime1 = System.nanoTime();
		final String dateString = StrUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
		// System.out.println(dateString + "==" + ip + "==" + mapDeviceType.get(ip) + "==" + plateNO);
		LOGGER.info(dateString + "==" + ip + "====" + plateNO);

		// 界面图片
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (outSmallImage != null) {
					LOGGER.info("出口小图片销毁图片");
					outSmallImage.dispose();
					lbl_outSmallImg.setBackgroundImage(null);
				}
				if (outBigImage != null) {
					LOGGER.info("出口大图片销毁图片");
					outBigImage.dispose();
					lbl_outBigImg.setBackgroundImage(null);
				}

				outSmallImage = CarparkUtils.getImage(smallImage, lbl_outSmallImg, shell);
				if (outSmallImage != null) {
					lbl_outSmallImg.setBackgroundImage(outSmallImage);
				}

				outBigImage = CarparkUtils.getImage(bigImage, lbl_outBigImg, shell);
				if (outBigImage != null) {
					lbl_outBigImg.setBackgroundImage(outBigImage);
				}

				text_real.setFocus();
				text_real.selectAll();
			}
		});
		model.setOutShowPlateNO(plateNO);
		model.setOutShowTime(dateString);
		//
		SingleCarparkDevice device = mapIpToDevice.get(ip);
		if (StrUtil.isEmpty(device)) {
			LOGGER.info("没有找到ip为：" + ip + "的设备");
			return;
		}
		SingleCarparkCarpark carpark = sp.getCarparkService().findCarparkById(device.getCarpark().getId());
		
		if (StrUtil.isEmpty(carpark)) {
			LOGGER.info("没有找到名字为：" + carpark + "的停车场");
			return;
		}
		model.setIp(ip);
		String bigImg = folder + "/" + bigImgFileName;
		String smallImg = folder + "/" + smallImgFileName;
		//
		if (StrUtil.isEmpty(plateNO)) {
			LOGGER.error("空的车牌");
			model.setSearchPlateNo(plateNO);
			model.setSearchBigImage(bigImg);
			model.setSearchSmallImage(smallImg);
			model.setHandSearch(true);
			model.setOutPlateNOEditable(true);
			return;
		}

		// 没有找到入场记录
		List<SingleCarparkInOutHistory> findByNoOut = sp.getCarparkInOutService().findByNoOut(plateNO,carpark);
		if (StrUtil.isEmpty(findByNoOut)) {
			LOGGER.info("没有找到车牌{}的入场记录", plateNO);
			model.setSearchPlateNo(plateNO);
			model.setSearchBigImage(bigImg);
			model.setSearchSmallImage(smallImg);
			model.setHandSearch(true);
			model.setOutPlateNOEditable(true);
			return;
		}
		SingleCarparkInOutHistory ch = findByNoOut.get(0);
		model.setInShowTime(StrUtil.formatDate(ch.getInTime(), "yyyy-MM-dd HH:mm:ss"));
		model.setInShowPlateNO(ch.getPlateNo());
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (CarInTask.inSmallImage != null) {
					LOGGER.info("进场小图片销毁图片");
					CarInTask.inSmallImage.dispose();
					CarInTask.inSmallImage = null;
					lbl_inSmallImg.setBackgroundImage(null);
				}
				if (CarInTask.inBigImage != null) {
					LOGGER.info("进场大图片销毁图片");
					CarInTask.inBigImage.dispose();
					CarInTask.inBigImage = null;
					lbl_inBigImg.setBackgroundImage(null);
				}
				CarInTask.inSmallImage = CarparkUtils.getImage(CarparkUtils.getImageByte(ch.getSmallImg()), lbl_inSmallImg, shell);
				if (CarInTask.inSmallImage != null) {
					lbl_inSmallImg.setBackgroundImage(CarInTask.inSmallImage);
				}

				CarInTask.inBigImage = CarparkUtils.getImage(CarparkUtils.getImageByte(ch.getBigImg()), lbl_inBigImg, shell);
				if (CarInTask.inBigImage != null) {
					lbl_inBigImg.setBackgroundImage(CarInTask.inBigImage);
				}
			}
		});

		presenter.showPlateNOToDevice(device, plateNO);
		//
		long nanoTime3 = System.nanoTime();
		List<SingleCarparkUser> findByNameOrPlateNo = sp.getCarparkUserService().findUserByPlateNo(plateNO);
		SingleCarparkUser user = StrUtil.isEmpty(findByNameOrPlateNo) ? null : findByNameOrPlateNo.get(0);
		String carType = "临时车";
		
		if (!StrUtil.isEmpty(user)) {
			Date userOutTime = new DateTime(user.getValidTo()).plusDays(user.getDelayDays()==null?0:user.getDelayDays()).toDate();
			if (userOutTime.after(date)) {
				carType="固定车";
			}
		}
		String roadType = device.getRoadType();
		LOGGER.info("车辆类型为：{}==通道类型为：{}", carType, roadType);
		// System.out.println("=====车辆类型为："+carType+"通道类型为："+roadType);
		long nanoTime2 = System.nanoTime();
		LOGGER.info(dateString + "==" + ip + "==" + device.getInType() + "==" + plateNO + "车辆类型：" + carType + "" + "保存图片：" + (nanoTime1 - nanoTime) + "==查找固定用户：" + (nanoTime2 - nanoTime3)
				+ "==界面操作：" + (nanoTime3 - nanoTime1));
		boolean equals = roadType.equals(DeviceRoadTypeEnum.固定车通道.name());

		if (carType.equals("固定车")) {
			if (fixCarOutProcess(ip, plateNO, date, device, user, roadType, equals, bigImg, smallImg)) {
				return;
			}
		} else {// 临时车操作
			// 固定车通道
			if (equals) {
				presenter.showContentToDevice(device, CarparkMainApp.FIX_ROAD, false);
				return;
			}
			tempCarOutProcess(ip, plateNO, device, date, bigImg, smallImg);
		}
	
	}
	
	/**
	 * 
	 * @param ip
	 * @param plateNO
	 * @param date
	 * @param device
	 * @param user
	 * @param roadType
	 * @param equals
	 * @param bigImg
	 * @param smallImg
	 * @return 返回true终止操作
	 */
	private boolean fixCarOutProcess(final String ip, final String plateNO, Date date, SingleCarparkDevice device, SingleCarparkUser user, String roadType, boolean equals, String bigImg,
			String smallImg) {
		String carType;
		carType = "固定车";
		if (!equals) {
			if (roadType.equals(DeviceRoadTypeEnum.临时车通道.name())) {
				presenter.showContentToDevice(device, CarparkMainApp.TEMP_ROAD, false);
				return true;
			}
		}
		String nowPlateNO = plateNO;
		// 固定车出场确认
		Boolean valueOf = Boolean
				.valueOf(mapSystemSetting.get(SystemSettingTypeEnum.固定车出场确认) == null ? SystemSettingTypeEnum.固定车出场确认.getDefaultValue() : mapSystemSetting.get(SystemSettingTypeEnum.固定车出场确认));

		if (valueOf) {
			model.setOutCheckClick(true);
			while (model.isOutCheckClick()) {
				int i = 0;
				try {
					if (i > 120) {
						return true;
					}
					Thread.sleep(500);
					i++;
				} catch (InterruptedException e) {
				}
			}
			nowPlateNO = model.getOutShowPlateNO();
		}
		//
		CarparkInOutServiceI carparkInOutService = sp.getCarparkInOutService();
		List<SingleCarparkInOutHistory> findByNoCharge = carparkInOutService.findByNoOut(nowPlateNO, device.getCarpark());
		Date validTo = user.getValidTo();
		Integer delayDays = user.getDelayDays() == null ? 0 : user.getDelayDays();

		Calendar c = Calendar.getInstance();
		c.setTime(validTo);
		c.add(Calendar.DATE, delayDays);
		Date time = c.getTime();

		if (StrUtil.getTodayBottomTime(time).before(date)) {
			presenter.showContentToDevice(device, CarparkMainApp.CAR_IS_ARREARS + StrUtil.formatDate(user.getValidTo(), CarparkMainApp.VILIDTO_DATE), false);
			LOGGER.info("车辆:{}已到期", nowPlateNO);
			if (Boolean.valueOf(mapSystemSetting.get(SystemSettingTypeEnum.固定车到期变临时车)==null?SystemSettingTypeEnum.固定车到期变临时车.getDefaultValue():mapSystemSetting.get(SystemSettingTypeEnum.固定车到期变临时车))) {
				tempCarOutProcess(ip, nowPlateNO, device, date, bigImg, smallImg);
			}
			return true;
		} else {
			c.setTime(validTo);
			c.add(Calendar.DATE, user.getRemindDays() == null ? 0 : user.getRemindDays() * -1);
			time = c.getTime();
			if (StrUtil.getTodayBottomTime(time).before(date)) {
				presenter.showContentToDevice(device, CarparkMainApp.CAR_OUT_MSG + ",剩余"+CarparkUtils.countDayByBetweenTime(date, user.getValidTo())+"天", true);
				LOGGER.info("车辆:{}即将到期", nowPlateNO);
			} else {
				presenter.showContentToDevice(device, CarparkMainApp.CAR_OUT_MSG, true);
			}
		}


		SingleCarparkInOutHistory singleCarparkInOutHistory = findByNoCharge.get(0);
		model.setPlateNo(nowPlateNO);
		model.setCarType(carType);
		model.setOutTime(date);
		Date inTime = singleCarparkInOutHistory.getInTime();
		model.setInTime(inTime);
		model.setShouldMony(0);
		model.setReal(0);
		model.setTotalTime(StrUtil.MinusTime2(inTime, date));
		singleCarparkInOutHistory.setOutTime(date);
		singleCarparkInOutHistory.setOperaName(model.getUserName());
		singleCarparkInOutHistory.setOutDevice(device.getName());
		singleCarparkInOutHistory.setOutPhotographType("自动");
		singleCarparkInOutHistory.setCarType(carType);
		singleCarparkInOutHistory.setOutBigImg(bigImg);
		singleCarparkInOutHistory.setOutSmallImg(smallImg);
		singleCarparkInOutHistory.setUserId(user.getId());
		singleCarparkInOutHistory.setUserName(user.getName());
		Date handPhotographDate = mapHandPhotograph.get(ip);
		if (!StrUtil.isEmpty(handPhotographDate)) {
			DateTime plusSeconds = new DateTime(handPhotographDate).plusSeconds(3);
			boolean after = plusSeconds.toDate().after(date);
			if (after)
				singleCarparkInOutHistory.setOutPhotographType("手动");
		}
		carparkInOutService.saveInOutHistory(singleCarparkInOutHistory);
		model.setTotalSlot(sp.getCarparkInOutService().findTotalSlotIsNow(model.getCarpark()));
		return false;
	}

	private void tempCarOutProcess(final String ip, final String plateNO, SingleCarparkDevice device, Date date, String bigImg, String smallImg) {
		CarparkInOutServiceI carparkInOutService = sp.getCarparkInOutService();
		List<SingleCarparkInOutHistory> findByNoCharge = carparkInOutService.findByNoOut(plateNO, device.getCarpark());
		if (!StrUtil.isEmpty(findByNoCharge)) {

			SingleCarparkInOutHistory singleCarparkInOutHistory = findByNoCharge.get(0);

			singleCarparkInOutHistory.setOutTime(date);
			singleCarparkInOutHistory.setOperaName(model.getUserName());
			singleCarparkInOutHistory.setOutDevice(device.getName());
			singleCarparkInOutHistory.setOutPhotographType("自动");
			singleCarparkInOutHistory.setOutBigImg(bigImg);
			singleCarparkInOutHistory.setOutSmallImg(smallImg);
			//
			Date handPhotographDate = mapHandPhotograph.get(ip);
			if (!StrUtil.isEmpty(handPhotographDate)) {
				DateTime plusSeconds = new DateTime(handPhotographDate).plusSeconds(3);
				boolean after = plusSeconds.toDate().after(date);
				if (after)
					singleCarparkInOutHistory.setOutPhotographType("手动");
			}
			Date inTime = singleCarparkInOutHistory.getInTime();

			// 临时车操作
			model.setPlateNo(plateNO);
			model.setCarType("临时车");
			model.setOutTime(date);
			model.setInTime(inTime);
			model.setTotalTime(StrUtil.MinusTime2(inTime, date));
			model.setHistory(singleCarparkInOutHistory);
			model.setShouldMony(0);
			model.setReal(0);

			Boolean isCharge = device.getCarpark().getIsCharge();
			if (StrUtil.isEmpty(isCharge) || !isCharge) {
				sp.getCarparkInOutService().saveInOutHistory(singleCarparkInOutHistory);
				presenter.showContentToDevice(device, CarparkMainApp.CAR_OUT_MSG, true);
			} else {
				CarTypeEnum carType = CarTypeEnum.SmallCar;
				if (mapTempCharge.keySet().size() > 1) {
					model.setComboCarTypeEnable(true);
					CarparkUtils.setFocus(carTypeSelectCombo);
					model.setSelectCarType(true);
					CarparkUtils.setComboSelect(carTypeSelectCombo, 0);
					while (!model.isBtnClick()) {
						try {
							if (model.getDisContinue()) {
								return;
							}
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					carType = getCarparkCarType(model.getCarparkCarType());
				} else if (mapTempCharge.keySet().size() == 1) {
					List<String> list = new ArrayList<>();
					list.addAll(mapTempCharge.keySet());
					carType = getCarparkCarType(list.get(0));
				}
				// model.setComboCarTypeEnable(false);
				float shouldMoney = presenter.countShouldMoney(device.getCarpark().getId(), carType, inTime, date);
				model.setShouldMony(shouldMoney);
				singleCarparkInOutHistory.setShouldMoney(shouldMoney);
				model.setReal(shouldMoney);
				singleCarparkInOutHistory.setFactMoney(shouldMoney);
				model.setCartypeEnum(carType);
				LOGGER.info("等待收费：车辆{}，停车场{}，车辆类型{}，进场时间{}，出场时间{}，停车：{}，应收费：{}元", plateNO, device.getCarpark(), carType, model.getInTime(), model.getOutTime(), model.getTotalTime(), shouldMoney);
				String s = "请缴费" + shouldMoney + "元";
				s = CarparkUtils.formatFloatString(s);

				String property = System.getProperty(CarparkMainApp.TEMP_CAR_AUTO_PASS);
				Boolean valueOf = Boolean.valueOf(property);
				// 临时车零收费是否自动出场
				Boolean tempCarNoChargeIsPass = Boolean.valueOf(mapSystemSetting.get(SystemSettingTypeEnum.临时车零收费是否自动出场));
				model.setBtnClick(true);
				LOGGER.info("等待收费");
				if (tempCarNoChargeIsPass) {
					if (shouldMoney > 0) {
						presenter.showContentToDevice(device, s, false);
						model.setChargeDevice(device);
						model.setChargeHistory(singleCarparkInOutHistory);
					} else {
						presenter.chargeCarPass(device, singleCarparkInOutHistory, false);
					}
				} else {
					presenter.showContentToDevice(device, s, false);
					model.setChargeDevice(device);
					model.setChargeHistory(singleCarparkInOutHistory);
				}
				if (valueOf) {
					singleCarparkInOutHistory.setFactMoney(shouldMoney);
					presenter.chargeCarPass(device, singleCarparkInOutHistory, false);
				}
			}
		}
	}
	private CarTypeEnum getCarparkCarType(String carparkCarType) {
		if (carparkCarType.equals("大车")) {
			return CarTypeEnum.BigCar;
		}
		if (carparkCarType.equals("小车")) {
			return CarTypeEnum.SmallCar;
		}
		if (carparkCarType.equals("摩托车")) {
			return CarTypeEnum.Motorcycle;
		}
		return CarTypeEnum.SmallCar;
	}
}