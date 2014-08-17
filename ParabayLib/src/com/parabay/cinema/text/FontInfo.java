package com.parabay.cinema.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Path;
import android.util.Log;

import com.parabay.cinema.text.tess.PGLU;
import com.parabay.cinema.text.tess.PGLUtessellator;
import com.parabay.cinema.text.tess.PGLUtessellatorCallbackAdapter;
import com.parabay.cinema.text.ttf.BoundingBox;
import com.parabay.cinema.text.ttf.CMAPEncodingEntry;
import com.parabay.cinema.text.ttf.CMAPTable;
import com.parabay.cinema.text.ttf.Glyph2D;
import com.parabay.cinema.text.ttf.GlyphData;
import com.parabay.cinema.text.ttf.GlyphTable;
import com.parabay.cinema.text.ttf.HeaderTable;
import com.parabay.cinema.text.ttf.HorizontalHeaderTable;
import com.parabay.cinema.text.ttf.HorizontalMetricsTable;
import com.parabay.cinema.text.ttf.NameRecord;
import com.parabay.cinema.text.ttf.NamingTable;
import com.parabay.cinema.text.ttf.OS2WindowsMetricsTable;
import com.parabay.cinema.text.ttf.PostScriptTable;
import com.parabay.cinema.text.ttf.TTFParser;
import com.parabay.cinema.text.ttf.TrueTypeFont;

public class FontInfo {

	public static final String TAG = "FontInfo";
	
	private TrueTypeFont ttf;
	private BoundingBox bbox;
	
	private String name;
	private String family;
    private boolean isSymbolic = false;
    private boolean isScript = false;
    private boolean isSerif = false;
    private float ascent;
    private float descent;
    private float capHeight;
    private float xHeight;
    private CMAPEncodingEntry uniMap;
    private int[] glyphWidths;
    
    private Map<Integer, String> charMap = new HashMap<Integer, String>();
    private Map<String, GlyphData> glyphMap = new HashMap<String, GlyphData>();
    private Map<Integer, Integer> glyphCharMap = new HashMap<Integer, Integer>();
    
	public FontInfo() {
		
	}
	
	public void initData(Context activity, String fontName) throws IOException {
		
		InputStream is = activity.getAssets().open(fontName);
		TTFParser parser = new TTFParser();
		
        this.ttf = parser.parseTTF( is );
        Log.i("FB", ttf.toString());
        
        NamingTable naming = ttf.getNaming();
        List<NameRecord> records = naming.getNameRecords();
        for( int i=0; i<records.size(); i++ )
        {
            NameRecord nr = records.get( i );
            if( nr.getNameId() == NameRecord.NAME_POSTSCRIPT_NAME )
            {
                this.name = nr.getString();
            }
            else if( nr.getNameId() == NameRecord.NAME_FONT_FAMILY_NAME )
            {
                this.family = nr.getString();
            }
        }

        OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
        
        switch( os2.getFamilyClass() )
        {
            case OS2WindowsMetricsTable.FAMILY_CLASS_SYMBOLIC:
                isSymbolic = true;
                break;
            case OS2WindowsMetricsTable.FAMILY_CLASS_SCRIPTS:
                isScript = true ;
                break;
            case OS2WindowsMetricsTable.FAMILY_CLASS_CLAREDON_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_FREEFORM_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_MODERN_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_OLDSTYLE_SERIFS:
            case OS2WindowsMetricsTable.FAMILY_CLASS_SLAB_SERIFS:
                isSerif = true;
                break;
            default:
                //do nothing
        }
        
        HeaderTable header = ttf.getHeader();
        this.bbox = new BoundingBox();
        
        float scaling = 1.0f; //1000f/header.getUnitsPerEm();
        this.bbox.setLowerLeftX( header.getXMin() * scaling );
        this.bbox.setLowerLeftY( header.getYMin() * scaling );
        this.bbox.setUpperRightX( header.getXMax() * scaling );
        this.bbox.setUpperRightY( header.getYMax() * scaling );

        HorizontalHeaderTable hHeader = ttf.getHorizontalHeader();
        this.ascent = (hHeader.getAscender() * scaling);
        this.descent =  hHeader.getDescender() * scaling;

        GlyphTable glyphTable = ttf.getGlyph();
        GlyphData[] glyphs = glyphTable.getGlyphs();

        PostScriptTable ps = ttf.getPostScript();
        String[] names = ps.getGlyphNames();
        
        if( names != null )
        {
            CMAPTable cmapTable = ttf.getCMAP();
            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
            CMAPEncodingEntry uniMap = null;
            
            for( int i=0; i<cmaps.length; i++ )
            {
                if( cmaps[i].getPlatformId() == CMAPTable.PLATFORM_WINDOWS) 
                {
                    int platformEncoding = cmaps[i].getPlatformEncodingId();
                    if ( CMAPTable.ENCODING_UNICODE == platformEncoding )
                    {
                        uniMap = cmaps[i];
                        break;
                    }
                }
            }
            
            if (null != uniMap) {
            	
            	int[] charCodes = uniMap.getGlyphIdToCharacterCode();
            	
            	for (int gid = 0; gid < charCodes.length; ++gid) 
                {
                    int charCode = charCodes[gid];
                    String name = names[gid];
                    
                    //Log.i(TAG, String.format("char->glyph mapping: %c -> %s", (char)charCode, name));
                    this.charMap.put(charCode, name);
                    this.glyphCharMap.put(charCode,  gid);
                }
            }
            
            for( int i=0; i<names.length; i++ )
            {
            	//Log.i("FB", names[i]);
            	
                if( glyphs != null && glyphs[i] != null && glyphs[i].getDescription() != null)
                {
                	this.glyphMap.put(names[i], glyphs[i]);
                }
                
                 //if we have a capital H then use that, otherwise use the
                //tallest letter
                if( names[i].equals( "H" ) )
                {
                    this.capHeight = ( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                }
                if( names[i].equals( "x" ) )
                {
                    this.xHeight = ( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                }
            }
        }
        
        HorizontalMetricsTable hMet = ttf.getHorizontalMetrics();
        this.glyphWidths = hMet.getAdvanceWidth();
 
        //Log.i(TAG, Arrays.toString(this.glyphWidths));
        
        if( ttf != null )
        {
            ttf.close();
        }        
		
	}
	
	public GlyphData getCharGlyph(String s) {
		
		char c = s.charAt(0);
		String glyphName = this.charMap.get((int)c);
		if (null == glyphName) {
			glyphName = s;
		}
		else {
			Log.i(TAG, String.format("Found glyph mapping: %c -> %s", c, glyphName));
		}
		
		GlyphData gd = this.glyphMap.get(glyphName);
		return gd;
	}

	public float getAscent() {
		return ascent;
	}

	public float getDescent() {
		return descent;
	}

	public BoundingBox getBbox() {
		return this.bbox;
	}

	public CMAPEncodingEntry getUniMap() {
		return uniMap;
	}

	public int[] getGlyphWidths() {
		return glyphWidths;
	}

	public int getGlyphWidth(char c) {
		
		int gid = this.glyphCharMap.get((int)c);
		int width = this.glyphWidths[0];
		if (glyphWidths.length > gid) {
			//width = glyphWidths[gid];
		}
		return width;
	}
}
