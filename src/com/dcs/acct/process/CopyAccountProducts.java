package com.dcs.acct.process;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.ProcessUtil;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MProductCategory;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Trx;

import com.dcsla.acct.base.CustomProcess;

public class CopyAccountProducts extends CustomProcess {

	String M_Product_Category_IDS = "";
	int C_AcctSchema_ID = 0;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals(MAcctSchema.COLUMNNAME_C_AcctSchema_ID)) {
				C_AcctSchema_ID = para[i].getParameterAsInt();
			} else if (name.equals("M_Product_Category_IDS")) {
				M_Product_Category_IDS = para[i].getParameterAsString();
			} else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

	} // prepare

	@Override
	protected String doIt() throws Exception {

		StringBuilder Where = new StringBuilder();
		Where.append("AD_Client_ID = ?");

		if (M_Product_Category_IDS != null)
			Where.append(" AND " + DB.inClauseForCSV("M_Product_Category_ID", M_Product_Category_IDS.toString()));

		List<MProductCategory> Cateogories = new Query(getCtx(), MProductCategory.Table_Name, Where.toString(),
				get_TrxName()).setParameters(getAD_Client_ID()).list();

		for (MProductCategory Category : Cateogories) {

			if (C_AcctSchema_ID > 0) {
				
				RunProcess(getCtx(), C_AcctSchema_ID, Category.get_ID(), get_TrxName());

			} else {
				MAcctSchema[] Schemas = MAcctSchema.getClientAcctSchema(getCtx(), getAD_Client_ID());

				for (MAcctSchema sc : Schemas) {
					
					RunProcess(getCtx(), sc.get_ID(), Category.get_ID(), get_TrxName());

				}

			}
		}

		return "OK";
	}

	public void RunProcess(Properties Ctx, int C_AcctSchema_ID, int M_Product_Category_ID, String TrxName) {

		MProcess process = new Query(getCtx(), MProcess.Table_Name, "classname = ?", TrxName)
				.setParameters("org.compiere.process.ProductCategoryAcctCopy").first();

		if (process != null) {

			ProcessInfo processInfo = new ProcessInfo(process.getName(), process.get_ID());
			MPInstance instance = new MPInstance(getCtx(), processInfo.getAD_Process_ID(), processInfo.getRecord_ID());
			instance.save();

			ProcessInfoParameter[] para = {
					new ProcessInfoParameter("M_Product_Category_ID", M_Product_Category_ID, null, null, null),
					new ProcessInfoParameter("C_AcctSchema_ID", C_AcctSchema_ID, null, null, null) };
			processInfo.setAD_Process_ID(process.get_ID());
			processInfo.setClassName(process.getClassname());
			processInfo.setAD_PInstance_ID(instance.getAD_PInstance_ID());
			processInfo.setParameter(para);

			ProcessUtil.startJavaProcess(getCtx(), processInfo, Trx.get(TrxName, false), true);
		} else {
			throw new AdempiereException("No Extiste el proceso");
		}
	}

}
