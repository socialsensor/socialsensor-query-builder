package eu.socialsensor.sfc.builder.input;

import java.util.List;
import java.util.Map;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;

public interface InputReader {
	
	public void run();
	
	public Map<FeedType,Object> getData();
	
	public Map<String,List<Feed>> createFeedsPerStream();
	
	public List<Feed> createFeeds();
}
