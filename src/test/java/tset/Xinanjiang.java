package tset;


public class Xinanjiang {
	/**
	 * TODO部分为修改地方
	 * 
	 */
	

	private double RS;
	private double RI;
	private double RG;

	private double[] QW; // 河网总入流

	// 产流参数
	private double B; // 张力水分布系数
	private double IM; // 不透水面积比例
	// 蒸散发参数
	private double K; // 蒸散发折算系数
	private double UM; // 上层张力水容量
	private double LM; // 下层张力水容量
	private double DM; // 深层张力水容量
	private double C; // 深层蒸发系数
	// 三水源划分参数
	private double SM; // 流域自由水平均容量
	private double EX; // 自由水容量曲线分布系数
	private double KI; // 壤中流出流系数
	private double KG; // 地下水出流系数
	
	// 汇流计算参数
	private double CI; // 壤中流消退系数
	private double CG; // 地下水消退系数
	private double CS; // 地表水消退系数
	
	private double KE; // 洪水演进参数
	private double XE;

	//TODO
	//初始条件设置
	private double WU = 0;
	private double WL = 40;
	private double WD = 20;
	private double FR = 0.05;
	private double S = 0.0;

	// 设置模型参数
	public void setModelPara(double[] xArray) {

		UM = xArray[0];
		LM = xArray[1];
		DM = xArray[2];
		B = xArray[3];
		IM = xArray[4];
		K = xArray[5];
		C = xArray[6];

		SM = xArray[7];
		EX = xArray[8];
		KI = xArray[9];
		KG = xArray[10];
		CI = xArray[11];
		CG = xArray[12];
		CS = xArray[13];

		KE = xArray[14];
		XE = xArray[15];

	}

	// 新安江模型主函数（计算单元流域出口流量）
	public double[] XAJmain(double[] paraArray, double[] P, double[] EM,
			double U) {
		//U=F/3.6/deta_t
		U = U * 1.0 / (1.0 * 3.6); // 径流深转换为流量的单位转换系数
		
		int len = P.length;
		
		double WM0, W0;
		double PE; // 净雨量
		double R, RIMP;
		double WMM, A, AU;
		double[] E = new double[3];
		double X, /* SS, */Q;
		int NN;
		double KID, KGD;
		double SMM, SMMF, SMF;
		double RSD, RSSD, RGD;
		RSD = 0.0;
		RSSD = 0.0;
		RGD = 0.0;
		double QRS, QRSS, QRG;
		double /* QRS0, */QRSS0, QRG0;
		// QRS0 = 0.0;
		QRSS0 = 0.0;
		QRG0 = 0.0;

		QW = new double[len];
		double[] subOut = new double[len];

		// 后面EM被修改，用EM2先将EM的值保存，最后再还原
		double[] EM2 = new double[len];
		for (int i = 0; i < len; i++)
			EM2[i] = EM[i];

		setModelPara(paraArray);

		// 主循环
		for (int i = 0; i < len; i++) {
			if (EM[i] < 0)
				EM[i] = 0;
			if (P[i] < 0)
				P[i] = 0;

			EM[i] = K * EM[i];

			WM0 = UM + LM + DM; // 流域某点的平均张力水容量
			W0 = WU + WL + WD; // 张力水含量
			PE = P[i] - EM[i]; // 净雨量

			R = 0;
			RIMP = 0;

			if (PE > 0) // 净雨量大于0，先计算产流量R
			{
				WMM = (1 + B) * WM0 / (1 - IM); // 流域最大点蓄水容量
				if (Math.abs(WM0 - W0) <= 0.0001) // 张力水含量约等于流域平均张力水容量
					A = WMM;
				else
					A = (WMM * (1 - Math.pow((1 - W0 / WM0), 1 / (B + 1))));

				if (PE + A < WMM)
					R = (PE - WM0 + W0 + WM0
							* Math.pow((1 - (PE + A) / WMM), 1 + B));
				else
					R = PE - WM0 + W0;

				RIMP = PE * IM; // 不透水面积上直接产流

			}

			// 三层蒸散发计算
			if (WU + P[i] > EM[i]) {
				E[0] = EM[i];
				E[1] = 0;
				E[2] = 0;
			} else {
				E[0] = WU + P[i]; // 上层全部蒸发
				E[1] = (EM[i] - E[0]) * WL / LM;
				if (WL <= C * LM) {
					E[1] = C * (EM[i] - E[0]);
					E[2] = 0;
				}
				if (WL >= C * (EM[i] - E[0])) // 下层水够蒸发
				{
					E[1] = C * (EM[i] - E[0]);
					E[2] = 0;
				} else // 下层水不够蒸发，蒸发深层水
				{
					E[1] = WL;
					E[2] = C * (EM[i] - E[0]) - E[1];
				}
//				if(WL >= C * LM){
//					E[1] = (EM[i] - E[0]) * WL / LM;
//					E[2] = 0;
//				}else{
//					E[1] = C * (EM[i] - E[0]);
//					if (WL >= C * (EM[i] - E[0])) // 下层水够蒸发
//					{
//						E[1] = C * (EM[i] - E[0]);
//						E[2] = 0;
//					} else // 下层水不够蒸发，蒸发深层水
//					{
//						E[1] = WL;
//						E[2] = C * (EM[i] - E[0]) - E[1];
//					}
//				}
				
			}

			// 更新上、下、深层张力水容量
			WU = WU + P[i] - R - E[0];
			WL = WL - E[1];
			WD = WD - E[2];
			if (WU > UM) {
				WL = WL + WU - UM;
				WU = UM;
			}
			if (WL > LM) {
				WD = WD + WL - LM;
				WL = LM;
			}

			X = FR;
			if (PE <= 0) // 净雨量小于等于0
			{
				RS = 0;
				RI = S * KI * FR;
				RG = S * FR * KG;
				// TODO
				S = S - (RI + RG) / FR;
			} else {
				FR = R / PE;
				//TODO
				if (FR > 1)
					FR = 1;
				if (FR == 0)
					FR = 0.001;
				
				S = X * S / FR;
				// SS = S;
				Q = R / FR;
				NN = (int) (Q / 5.0) + 1;
				if (Q % 5 == 0)
					NN = NN - 1;
				if (NN == 0) {
					NN = 1;
				}
				Q = (Q / (1.0 * NN));

				// 出流系数换算
				KID = ((1 - Math.pow((1 - (KI + KG)), 1.0 / NN)) / (1 + KG / KI));
				KGD = (KID * KG / KI);

				RS = 0;
				RI = 0;
				RG = 0;
				SMM = (1 + EX) * SM; // 流域最大自由水容量
				if (EX <= 0.0001) // EX约为0，表明流域自由水容量分布均匀
					SMMF = SMM; // 产流面积上最大点自由水容量
				else
					SMMF = (SMM * (1 - Math.pow(1 - FR, 1 / EX)));

				SMF = SMMF / (1 + EX); // 流域平均蓄水容量深

				for (int j = 0; j < NN; j++) {
					if (S > SMF)
						S = SMF;
					AU = (SMMF * (1 - Math.pow((1 - S / SMF), 1 / (1 + EX))));
					if (Q + AU <= 0) {
						RSD = 0;
						RSSD = 0;
						RGD = 0;
						S = 0;
					} else if (Q + AU >= SMMF) {
						RSD = (Q + S - SMF) * FR;
						RSSD = SMF * KID * FR;
						RGD = SMF * FR * KGD;
						S = SMF - (RSSD + RGD) / FR;
					} else if (Q + AU < SMMF) {
						RSD = ((Q - SMF + S + SMF
								* Math.pow(1 - (Q + AU) / SMMF, EX + 1)) * FR);
						RSSD = (S + Q - RSD / FR) * KID * FR;
						RGD = (S + Q - RSD / FR) * KGD * FR;
						S = S + Q - (RSD + RSSD + RGD) / FR;
					}
					RS = RS + RSD;
					RI = RI + RSSD;
					RG = RG + RGD;
				}

			}

			RS = RS * (1 - IM);
			RI = RI * (1 - IM);
			RG = RG * (1 - IM);

			QRS = (RS + RIMP) * U;
			QRSS = QRSS0 * CI + RI * (1 - CI) * U;
			QRG = QRG0 * CG + RG * (1 - CG) * U;
			QW[i] = QRS + QRSS + QRG;

			
			if (i != 0) {
				subOut[i] = CS * subOut[i - 1] + (1 - CS) * QW[i - 1];
			} else
				subOut[0] = QW[0];

			QRSS0 = QRSS;
			QRG0 = QRG;

		}

		for ( int i = 0; i < len; i++)
			EM[i] = EM2[i];

		// 河道汇流----马斯京根法 KE XE

		double[] output = routing(subOut, 2,KE, XE);
		for (int k = 0; k < len; k++) {
			if (output[k] <= 0) {
				output[k] = 0;
			}
		}

		return output;
//		return subOut;
	}


/*	public double[] routing(double[] inflow, double KE, double XE) {
		int i, j, k;
		double C0, C1, C2;
		int len = inflow.length;
		double[] outflow = new double[len];

		double DTT = 1.0;
		// ����C0,C1,C2
		C0 = (0.5 * DTT - KE * XE) / (0.5 * DTT + KE - KE * XE);
		C1 = (0.5 * DTT + KE * XE) / (0.5 * DTT + KE - KE * XE);
		C2 = 1 - C0 - C1;

		outflow[0] = inflow[0];

		for (j = 1; j < len; j++) {
			outflow[j] = C0 * inflow[j] + C1 * inflow[j - 1] + C2
					* outflow[j - 1];
		}

		return outflow;
	}*/
	// 河道洪水演算函数
	// inflow为输入流量过程，n为划分的河段数
	public double[] routing(double[] inflow, int n, double KE, double XE)
	{
		int i, j, k;
		double C0, C1, C2;
		int len = inflow.length;
		double[] outflow = new double[len];
		
		double DT = 1.0;
		//����C0,C1,C2
		C0 = (0.5 * DT - KE * XE) / (0.5 * DT + KE - KE * XE);
		C1 = (0.5 * DT + KE * XE) / (0.5 * DT + KE - KE * XE);
		C2 = 1 - C0 - C1;
		
		if(n == 0)
			outflow = inflow;
		
		for(i = 0; i < n; i ++)
		{
			outflow[0] = inflow[0];
			if(i == 0)
			{
				for(j = 1; j < len; j ++)
				{
					outflow[j] = C0 * inflow[j] + C1 * inflow[j - 1] + C2 * outflow[j - 1];
				}
			}
			else
			{
				for(k = 1; k < len; k ++)
				{
					outflow[k] = C0 * outflow[k] + C1 * outflow[k - 1] + C2 * outflow[k - 1];
				}
			}
		}
		
		
		return outflow;
	}

}
