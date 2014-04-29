package com.gjk.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.util.Pair;
import android.util.Patterns;
import android.widget.TextView;

public class TextUtil {
	
	public static class Patterns {
		public static final Pattern MENTION = Pattern.compile("\\[\\s*[\\w\\s-?']+\\s*\\]");
		public static final Pattern HASHTAG = Pattern.compile("#\\w+");
	}

	public static class Linkify {
		
	    /**
	     *  Bit field indicating mentions should be matched in methods that
	     *  take an options mask
	     */
	    public static final int MENTION = 0x10;

	    /**
	     *  Bit field indicating that hashtags should be matched in methods that
	     *  take an options mask
	     */
	    public static final int HASHTAG = 0x20;

	    /**
	     *  Bit mask indicating that all available patterns should be matched in
	     *  methods that take an options mask
	     */
	    public static final int MILU_ALL = MENTION | HASHTAG;

	    private static final class MentionMatchFilter implements MatchFilter {
	    	Map<Long, Pair<String, Long>> mIdToNameMap;
	    	
	    	public MentionMatchFilter(Map<Long, Pair<String, Long>> map) {
	    		mIdToNameMap = map;
	    	}

			@Override
			public boolean acceptMatch(CharSequence s, int start, int end, int prevMatchCount) {
				if(mIdToNameMap == null)
					return false;
				
				Iterator<Entry<Long, Pair<String, Long>>> it = mIdToNameMap.entrySet().iterator();
				int currMatch = 0;
				while(it.hasNext()) {
					Entry<Long, Pair<String, Long>> entry = it.next();
					String strippedUrl = s.subSequence(start, end).toString().replace("[", "").replace("]", "").trim();
					if(entry.getValue().first.equalsIgnoreCase(strippedUrl)) {
						if(currMatch >= prevMatchCount) {
							return true;
						} else
							currMatch++;
					}
				}
				return false;
			}
	    }
	    
	    private static final class MentionTransformFilter implements TransformFilter {

	    	Map<Long, Pair<String, Long>> mIdToNameMap;
	    	
	    	public MentionTransformFilter(Map<Long, Pair<String, Long>> map) {
	    		mIdToNameMap = map;
	    	}
	    	
			@Override
			public String transformUrl(Matcher match, String url, int prevMatchCount) {
				if(mIdToNameMap == null)
					return url;
				
				Iterator<Entry<Long, Pair<String, Long>>> it = mIdToNameMap.entrySet().iterator();
				int currMatch = 0;
				while(it.hasNext()) {
					Entry<Long, Pair<String, Long>> entry = it.next();
					String strippedUrl = url.replace("[", "").replace("]", "").trim();
					if(entry.getValue().first.equalsIgnoreCase(strippedUrl)) {
						if(currMatch >= prevMatchCount) {
							url = "id=" + entry.getKey() + "&name=" + entry.getValue().first + "&entity_type=" + entry.getValue().second;
							break;
						} else
							currMatch++;
					}
				}
				return url;
			}
	    }
	    
		/**
		 *  MatchFilter enables client code to have more control over
		 *  what is allowed to match and become a link, and what is not.
		 *
		 *  For example:  when matching web urls you would like things like
		 *  http://www.example.com to match, as well as just example.com itelf.
		 *  However, you would not want to match against the domain in
		 *  support@example.com.  So, when matching against a web url pattern you
		 *  might also include a MatchFilter that disallows the match if it is
		 *  immediately preceded by an at-sign (@).
		 */
		public interface MatchFilter {
			/**
			 *  Examines the character span matched by the pattern and determines
			 *  if the match should be turned into an actionable link.
			 *
			 *  @param s        The body of text against which the pattern
			 *                  was matched
			 *  @param start    The index of the first character in s that was
			 *                  matched by the pattern - inclusive
			 *  @param end      The index of the last character in s that was
			 *                  matched - exclusive
			 *
			 *  @return         Whether this match should be turned into a link
			 */
			boolean acceptMatch(CharSequence s, int start, int end, int prevMatchCount);
		}

		/**
		 *  TransformFilter enables client code to have more control over
		 *  how matched patterns are represented as URLs.
		 *
		 *  For example:  when converting a phone number such as (919)  555-1212
		 *  into a tel: URL the parentheses, white space, and hyphen need to be
		 *  removed to produce tel:9195551212.
		 */
		public interface TransformFilter {
			/**
			 *  Examines the matched text and either passes it through or uses the
			 *  data in the Matcher state to produce a replacement.
			 *
			 *  @param match    The regex matcher state that found this URL text
			 *  @param url      The text that was matched
			 *
			 *  @return         The transformed form of the URL
			 */
			String transformUrl(final Matcher match, String url, int prevMatchCount);
		}
		
		
	    private static final void addLinkMovementMethod(TextView t) {
	        MovementMethod m = t.getMovementMethod();

	        if ((m == null) || !(m instanceof LinkMovementMethod)) {
	            if (t.getLinksClickable()) {
	                t.setMovementMethod(LinkMovementMethod.getInstance());
	            }
	        }
	    }
		

	    /**
	     *  Scans the text of the provided TextView and turns all occurrences of
	     *  the link types indicated in the mask into clickable links.  If matches
	     *  are found the movement method for the TextView is set to
	     *  LinkMovementMethod.
	     */
	    public static final boolean addLinks(TextView text, int mask, Map<Long, Pair<String, Long>> profileMap) {
	        if (mask == 0) {
	            return false;
	        }

	        CharSequence t = text.getText();

	        if (t instanceof Spannable) {
	            if (addLinks((Spannable) t, mask, profileMap)) {
	                addLinkMovementMethod(text);
	                return true;
	            }

	            return false;
	        } else {
	            SpannableString s = SpannableString.valueOf(t);

	            if (addLinks(s, mask, profileMap)) {
	                addLinkMovementMethod(text);
	                text.setText(s);

	                return true;
	            }

	            return false;
	        }
	    }
	    
	    /**
	     *  Scans the text of the provided Spannable and turns all occurrences
	     *  of the link types indicated in the mask into clickable links.
	     *  If the mask is nonzero, it also removes any existing URLSpans
	     *  attached to the Spannable, to avoid problems if you call it
	     *  repeatedly on the same text.
	     */
	    public static final boolean addLinks(Spannable text, int mask, Map<Long, Pair<String, Long>> profileMap) {
	        if (mask == 0) {
	            return false;
	        }

	        ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();

	        if ((mask & MENTION) != 0 && profileMap != null && !profileMap.isEmpty()) {
	            gatherLinks(links, text, Patterns.MENTION,
	                new String[] { "milu://profile?" },
	                new MentionMatchFilter(profileMap), new MentionTransformFilter(profileMap));
	        }

	        if ((mask & HASHTAG) != 0) {
	            gatherLinks(links, text, Patterns.HASHTAG,
	                new String[] { "http://www.twitter.com/s/" },
	                null, null);
	        }

	        pruneOverlaps(links);

	        if (links.size() == 0) {
	            return false;
	        }

	        for (LinkSpec link: links) {
	            applyLink(link.url, link.start, link.end, text);
	        }

	        return true;
	    }

		private static final void applyLink(String url, int start, int end, Spannable text) {
			URLSpan span = new URLSpan(url);

			text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
	    private static final void gatherLinks(ArrayList<LinkSpec> links,
	            Spannable s, Pattern pattern, String[] schemes,
	            MatchFilter matchFilter, TransformFilter transformFilter) {
	        Matcher m = pattern.matcher(s);

	        HashMap<String, Integer> nameCount = new HashMap<String, Integer>();
	        while (m.find()) {
	            int start = m.start();
	            int end = m.end();
	            
                String matchingUrl = m.group(0);
                String key = matchingUrl.toLowerCase();
                int currCount = nameCount.get(key) == null ? 0 : nameCount.get(key);
                
	            if (matchFilter == null || matchFilter.acceptMatch(s, start, end, currCount)) {
	            	LinkSpec spec = new LinkSpec();   
	                String url = makeUrl(matchingUrl, schemes, m, transformFilter, currCount);
	                spec.url = url;
	                spec.start = start;
	                spec.end = end;

	                links.add(spec);
	                
	                nameCount.put(key, currCount + 1);
	            }
	        }
	    }

		private static final String makeUrl(String url, String[] prefixes,
				Matcher m, TransformFilter filter, int urlCount) {
			if (filter != null) {
				url = filter.transformUrl(m, url, urlCount);
			}

			boolean hasPrefix = false;

			for (int i = 0; i < prefixes.length; i++) {
				if (url.regionMatches(true, 0, prefixes[i], 0,
						prefixes[i].length())) {
					hasPrefix = true;

					// Fix capitalization if necessary
					if (!url.regionMatches(false, 0, prefixes[i], 0,
							prefixes[i].length())) {
						url = prefixes[i] + url.substring(prefixes[i].length());
					}

					break;
				}
			}

			if (!hasPrefix) {
				url = prefixes[0] + url;
			}

			return url;
		}
		
	    private static final void pruneOverlaps(ArrayList<LinkSpec> links) {
	        Comparator<LinkSpec>  c = new Comparator<LinkSpec>() {
	            public final int compare(LinkSpec a, LinkSpec b) {
	                if (a.start < b.start) {
	                    return -1;
	                }

	                if (a.start > b.start) {
	                    return 1;
	                }

	                if (a.end < b.end) {
	                    return 1;
	                }

	                if (a.end > b.end) {
	                    return -1;
	                }

	                return 0;
	            }

	            public final boolean equals(Object o) {
	                return false;
	            }
	        };

	        Collections.sort(links, c);

	        int len = links.size();
	        int i = 0;

	        while (i < len - 1) {
	            LinkSpec a = links.get(i);
	            LinkSpec b = links.get(i + 1);
	            int remove = -1;

	            if ((a.start <= b.start) && (a.end > b.start)) {
	                if (b.end <= a.end) {
	                    remove = i + 1;
	                } else if ((a.end - a.start) > (b.end - b.start)) {
	                    remove = i + 1;
	                } else if ((a.end - a.start) < (b.end - b.start)) {
	                    remove = i;
	                }

	                if (remove != -1) {
	                    links.remove(remove);
	                    len--;
	                    continue;
	                }

	            }

	            i++;
	        }
	    }
	    
	    public static void removeSpans(TextView textView) {
	        CharSequence t = textView.getText();

	        
	        if(t instanceof Spannable) {
	        	Spannable span = (Spannable)t;
	        	URLSpan[] old = span.getSpans(0, span.length(), URLSpan.class);
	        	for(int i=old.length - 1; i >=0; i--) {
	        		span.removeSpan(old[i]);
	        	}
	        } else {
	        	SpannableString span = SpannableString.valueOf(t);
	        	URLSpan[] old = span.getSpans(0, span.length(), URLSpan.class);
	        	for(int i=old.length - 1; i >=0; i--) {
	        		span.removeSpan(old[i]);
	        	}
	        }
	    }
	}
	
	static class LinkSpec {
	    String url;
	    int start;
	    int end;
	}
}
