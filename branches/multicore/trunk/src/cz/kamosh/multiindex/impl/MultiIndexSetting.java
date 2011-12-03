package cz.kamosh.multiindex.impl;

public final class MultiIndexSetting {

	private MultiIndexSetting() {};
	
	static MultiIndexSetting instance = new MultiIndexSetting();
	
	public static MultiIndexSetting getInstance() {
		return instance;
	}
	
	private boolean useParalellization = true;

	public boolean isUseParalellization() {
		return useParalellization;
	}

	public void setUseParalellization(boolean useParalellization) {
		this.useParalellization = useParalellization;
	}
		
}
