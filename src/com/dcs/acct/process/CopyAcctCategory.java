package com.dcs.acct.process;

import java.util.logging.Level;

import org.compiere.model.MAcctSchema;
import org.compiere.model.MProductCategory;
import org.compiere.model.MProductCategoryAcct;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;

import com.dcsla.acct.base.CustomProcess;

public class CopyAcctCategory extends CustomProcess {

	int C_AcctSchema_ID = 0;
	int C_AcctSchema_To_ID = 0;

	/**
	 * Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals(MAcctSchema.COLUMNNAME_C_AcctSchema_ID)) {
				C_AcctSchema_ID = para[i].getParameterAsInt();
			} else if (name.equals("C_AcctSchema_To_ID")) {
				C_AcctSchema_To_ID = para[i].getParameterAsInt();
			} else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}

	} // prepare

	@Override
	protected String doIt() throws Exception {

		MProductCategoryAcct ca = MProductCategoryAcct.get(getCtx(), getRecord_ID(), C_AcctSchema_ID, get_TrxName());
		MProductCategoryAcct caToCopy = getAccounting(getRecord_ID(), C_AcctSchema_To_ID);
		int FromSchema_ID = caToCopy.getC_AcctSchema_ID();
		MProductCategoryAcct.copyValues(ca, caToCopy);
		caToCopy.setC_AcctSchema_ID(FromSchema_ID);
		caToCopy.saveEx();

		return "Copied";
	}
	
	MProductCategoryAcct getAccounting(int Category_ID, int Schema_ID) {
		
		String where = MProductCategory.COLUMNNAME_M_Product_Category_ID+"= ? AND "+MAcctSchema.COLUMNNAME_C_AcctSchema_ID+"= ? AND "+MAcctSchema.COLUMNNAME_AD_Client_ID+"= ? ";
		
		MProductCategoryAcct acct = new Query(getCtx(), MProductCategoryAcct.Table_Name, where, get_TrxName())
				.setParameters(Category_ID, Schema_ID, getAD_Client_ID())
				.first();
		
		return acct;
		
		
		
	}

}
