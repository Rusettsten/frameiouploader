package org.muny.frameiouploader.utility;

import static com.diogonunes.jcolor.Attribute.BLACK_TEXT;
import static com.diogonunes.jcolor.Attribute.BOLD;
import static com.diogonunes.jcolor.Attribute.GREEN_TEXT;
import static com.diogonunes.jcolor.Attribute.RED_BACK;
import static com.diogonunes.jcolor.Attribute.UNDERLINE;
import static com.diogonunes.jcolor.Attribute.WHITE_BACK;
import static com.diogonunes.jcolor.Attribute.WHITE_TEXT;
import static com.diogonunes.jcolor.Attribute.YELLOW_BACK;

import com.diogonunes.jcolor.AnsiFormat;


public class ConsoleHelper {

	/*
	 * VARIABLES
	 */
	public static AnsiFormat fGood = new AnsiFormat(GREEN_TEXT());
	public static AnsiFormat fInformation = new AnsiFormat(WHITE_TEXT());
	public static AnsiFormat fWarning = new AnsiFormat(BLACK_TEXT(), YELLOW_BACK());
	public static AnsiFormat fError = new AnsiFormat(BLACK_TEXT(), RED_BACK(), BOLD());
	public static AnsiFormat fSeperator = new AnsiFormat(WHITE_TEXT(), BOLD());
	public static AnsiFormat fTitle = new AnsiFormat(BLACK_TEXT(), WHITE_BACK(), BOLD());
	public static AnsiFormat fSubTitle = new AnsiFormat(WHITE_TEXT(), UNDERLINE(), BOLD());
	
	
	/*
	 * METHODS
	 */
	public static void outputGood(String text) {
		System.out.println(fGood.format(text));
	}
	
	public static void outputInformation(String text) {
		System.out.println(fInformation.format(text));
	}
	
	public static void outputWarning(String text) {
		System.out.println(fWarning.format(text));
	}
	public static void outputWarningSameLine(String text) {
		System.out.print(fWarning.format(text));
	}
	
	public static void outputError(String text) {
		System.out.println(fError.format(text));
	}
	
	public static void outputSeperator() {
		System.out.println("");
		System.out.println(fSeperator.format("<><><><><><><><><><><><><><><><><><><><><><><><><><><><>"));
		System.out.println("");
	}
	
	public static void outputTitle(String text) {
		System.out.println(fTitle.format(text));
	}
	
	public static void outputSubTitle(String text) {
		System.out.println(fSubTitle.format(text));
	}
}
