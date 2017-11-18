package infoextra.beta3;

import java.util.List;
import java.util.Map;

public abstract class Seed {
	abstract public List<Map<String, String>> getUpdateResults();
	abstract public String updateMark();
	abstract public boolean setConfig(Map<String, String> config);
	abstract public String name();
}
