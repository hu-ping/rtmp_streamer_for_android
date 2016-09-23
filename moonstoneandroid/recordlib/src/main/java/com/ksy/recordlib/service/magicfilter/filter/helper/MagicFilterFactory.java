package com.ksy.recordlib.service.magicfilter.filter.helper;

import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicAmaroFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicAntiqueFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicBlackCatFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicBrannanFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicBrooklynFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicCalmFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicCoolFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicCrayonFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicEarlyBirdFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicEmeraldFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicEvergreenFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicFairytaleFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicFreudFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicHealthyFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicHefeFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicHudsonFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicImageAdjustFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicInkwellFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicKevinFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicLatteFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicLomoFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicN1977Filter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicNashvilleFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicNostalgiaFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicPixarFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicRiseFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicRomanceFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSakuraFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSierraFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSketchFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSkinWhitenFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSunriseFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSunsetFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSutroFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicSweetsFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicTenderFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicToasterFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicValenciaFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicWaldenFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicWarmFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicWhiteCatFilter;
import com.ksy.recordlib.service.magicfilter.filter.advanced.MagicXproIIFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageContrastFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageExposureFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageHueFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageSaturationFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageSharpenFilter;

public class MagicFilterFactory{
	
	private static MagicFilterType filterType = MagicFilterType.NONE;
	
	public static GPUImageFilter initFilters(MagicFilterType type){
		filterType = type;
		switch (type) {
		case WHITECAT:
			return new MagicWhiteCatFilter();
		case BLACKCAT:
			return new MagicBlackCatFilter();
		case SKINWHITEN:
			return new MagicSkinWhitenFilter();
		case ROMANCE:
			return new MagicRomanceFilter();
		case SAKURA:
			return new MagicSakuraFilter();
		case AMARO:
			return new MagicAmaroFilter();
		case WALDEN:
			return new MagicWaldenFilter();
		case ANTIQUE:
			return new MagicAntiqueFilter();
		case CALM:
			return new MagicCalmFilter();
		case BRANNAN:
			return new MagicBrannanFilter();
		case BROOKLYN:
			return new MagicBrooklynFilter();
		case EARLYBIRD:
			return new MagicEarlyBirdFilter();
		case FREUD:
			return new MagicFreudFilter();
		case HEFE:
			return new MagicHefeFilter();
		case HUDSON:
			return new MagicHudsonFilter();
		case INKWELL:
			return new MagicInkwellFilter();
		case KEVIN:
			return new MagicKevinFilter();
		case LOMO:
			return new MagicLomoFilter();
		case N1977:
			return new MagicN1977Filter();
		case NASHVILLE:
			return new MagicNashvilleFilter();
		case PIXAR:
			return new MagicPixarFilter();
		case RISE:
			return new MagicRiseFilter();
		case SIERRA:
			return new MagicSierraFilter();
		case SUTRO:
			return new MagicSutroFilter();
		case TOASTER2:
			return new MagicToasterFilter();
		case VALENCIA:
			return new MagicValenciaFilter();
		case XPROII:
			return new MagicXproIIFilter();
		case EVERGREEN:
			return new MagicEvergreenFilter();
		case HEALTHY:
			return new MagicHealthyFilter();
		case COOL:
			return new MagicCoolFilter();
		case EMERALD:
			return new MagicEmeraldFilter();
		case LATTE:
			return new MagicLatteFilter();
		case WARM:
			return new MagicWarmFilter();
		case TENDER:
			return new MagicTenderFilter();
		case SWEETS:
			return new MagicSweetsFilter();
		case NOSTALGIA:
			return new MagicNostalgiaFilter();
		case FAIRYTALE:
			return new MagicFairytaleFilter();
		case SUNRISE:
			return new MagicSunriseFilter();
		case SUNSET:
			return new MagicSunsetFilter();
		case CRAYON:
			return new MagicCrayonFilter();
		case SKETCH:
			return new MagicSketchFilter();
		//image adjust
		case BRIGHTNESS:
			return new GPUImageBrightnessFilter();
		case CONTRAST:
			return new GPUImageContrastFilter();
		case EXPOSURE:
			return new GPUImageExposureFilter();
		case HUE:
			return new GPUImageHueFilter();
		case SATURATION:
			return new GPUImageSaturationFilter();
		case SHARPEN:
			return new GPUImageSharpenFilter();
		case IMAGE_ADJUST:
			return new MagicImageAdjustFilter();
		default:
			return null;
		}
	}
	
	public MagicFilterType getCurrentFilterType(){
		return filterType;
	}
}
