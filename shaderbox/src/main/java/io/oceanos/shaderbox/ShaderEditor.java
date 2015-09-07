/*
 *    Copyright 2015 Rui Gil
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.oceanos.shaderbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderEditor extends EditText  {

	private static final Pattern symbols = Pattern.compile( ";|,|\\(|\\)|\\{|\\}|\\*|\\+|\\-|/|=");

	private static final Pattern numbers = Pattern.compile( "\\b(\\d*[.]?\\d+)\\b" );

	private static final Pattern keywords = Pattern.compile(
		"\\b(attribute|const|uniform|varying|break|continue|"+
		"do|for|while|if|else|in|out|inout|float|int|void|bool|true|false|"+
		"lowp|mediump|highp|precision|invariant|discard|return|mat2|mat3|"+
		"mat4|vec2|vec3|vec4|ivec2|ivec3|ivec4|bvec2|bvec3|bvec4|sampler2D|"+
		"samplerCube|struct|gl_Vertex|gl_FragCoord|gl_FragColor)\\b" );

	private static final Pattern builtins = Pattern.compile(
		"\\b(radians|degrees|sin|cos|tan|asin|acos|atan|pow|"+
		"exp|log|exp2|log2|sqrt|inversesqrt|abs|sign|floor|ceil|fract|mod|"+
		"min|max|clamp|mix|step|smoothstep|length|distance|dot|cross|"+
		"normalize|faceforward|reflect|refract|matrixCompMult|lessThan|"+
		"lessThanEqual|greaterThan|greaterThanEqual|equal|notEqual|any|all|"+
		"not|dFdx|dFdy|fwidth|texture2D|texture2DProj|texture2DLod|"+
		"texture2DProjLod|textureCube|textureCubeLod)\\b" );

	private static final Pattern comments = Pattern.compile( "(?://.*)|(/\\*(?:.|[\\n\\r])*?\\*/)", Pattern.MULTILINE);

	private static final Pattern macros = Pattern.compile("#.*$", Pattern.MULTILINE);

	private final int
			COLOR_ERROR = getResources().getColor(R.color.editor_color_error),
			COLOR_NUMBER = getResources().getColor(R.color.editor_color_number),
			COLOR_KEYWORD = getResources().getColor(R.color.editor_color_keyword),
			COLOR_BUILTIN = getResources().getColor(R.color.editor_color_builtin),
			COLOR_COMMENT = getResources().getColor(R.color.editor_color_comment),
			COLOR_SYMBOL = getResources().getColor(R.color.editor_color_symbol),
			COLOR_MACROS = getResources().getColor(R.color.editor_color_macros);

	private ScrollView scroll;
	private UpdateHighlight updateHighlight;

	public ShaderEditor(Context context) {
		super( context );
		setFilters(new InputFilter[]{new IdentFilter()});
		updateHighlight = new UpdateHighlight();
		updateHighlight.start();
	}

	public ShaderEditor(Context context, AttributeSet attrs) {
		super( context, attrs);
		setFilters(new InputFilter[]{new IdentFilter()});
		updateHighlight = new UpdateHighlight();
		updateHighlight.start();
	}


	public void setText(String text) {
		SpannableStringBuilder ss = new SpannableStringBuilder(text);
		ss.setSpan(new EditorLineNumberSpan(), 0, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		addTextChangedListener(updateHighlight);
		super.setText(ss);
	}

	private void clearSpans(int offsetCharStart,int offsetCharEnd) {
		Editable e = getEditableText();
		// clear foreground spans
		for (ForegroundColorSpan span :
				e.getSpans(offsetCharStart, offsetCharEnd, ForegroundColorSpan.class))
			e.removeSpan(span);
	}

	private void highlight(int offsetCharStart, int offsetCharEnd) {
		Editable e = getEditableText();

		CharSequence textToHighlight = e.subSequence(offsetCharStart, offsetCharEnd);

		{ // numbers
				Matcher m = numbers.matcher(textToHighlight);
				while (m.find())
					e.setSpan(
							new ForegroundColorSpan(COLOR_NUMBER),
							offsetCharStart + m.start(),
							offsetCharStart + m.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
					);
		}

		{ // keywords
			Matcher m = keywords.matcher(textToHighlight);
			while (m.find())
				e.setSpan(
						new ForegroundColorSpan(COLOR_KEYWORD),
						offsetCharStart + m.start(),
						offsetCharStart + m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				);
		}

		{ // builtins
			Matcher m = builtins.matcher(textToHighlight);
			while (m.find())
				e.setSpan(
						new ForegroundColorSpan(COLOR_BUILTIN),
						offsetCharStart + m.start(),
						offsetCharStart + m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				);
		}
		{ // symbols
			Matcher m = symbols.matcher(textToHighlight);
			while (m.find())
				e.setSpan(
						new ForegroundColorSpan(COLOR_SYMBOL),
						offsetCharStart + m.start(),
						offsetCharStart + m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				);
		}
		{ // macros
			Matcher m = macros.matcher(textToHighlight);
			while (m.find()) {
				e.setSpan(
						new ForegroundColorSpan(COLOR_MACROS),
						offsetCharStart + m.start(),
						offsetCharStart + m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				);
			}
		}
		{ // comments
			Matcher m = comments.matcher(textToHighlight);
			while (m.find()) {
				e.setSpan(
						new ForegroundColorSpan(COLOR_COMMENT),
						offsetCharStart + m.start(),
						offsetCharStart + m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				);
			}
		}
	}

	public void setErrorLine(int errorLine) {
		Editable e = getEditableText();
		for (ErrorLineSpan span :
				e.getSpans(0,e.length(),ErrorLineSpan.class)) e.removeSpan(span);

		final int start = getLayout().getLineStart(errorLine);
		final int end = getLayout().getLineEnd(errorLine);

		e.setSpan(new ErrorLineSpan(errorLine), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public void setScrollView(final ScrollView scroll) {
		this.scroll = scroll;
		scroll.getViewTreeObserver().addOnScrollChangedListener(updateHighlight);
		scroll.getViewTreeObserver().addOnGlobalLayoutListener(updateHighlight);
	}

	public void onResume() {
		if (updateHighlight == null) {
			updateHighlight = new UpdateHighlight();
			if (scroll != null) {
				scroll.getViewTreeObserver().addOnScrollChangedListener(updateHighlight);
				scroll.getViewTreeObserver().addOnGlobalLayoutListener(updateHighlight);
			}
			addTextChangedListener(updateHighlight);
			updateHighlight.start();
		}
	}

	public void onPause() {
		if (scroll != null) {
			scroll.getViewTreeObserver().removeOnScrollChangedListener(updateHighlight);
			scroll.getViewTreeObserver().removeOnGlobalLayoutListener(updateHighlight);
		}
		removeTextChangedListener(updateHighlight);
		updateHighlight.interrupt();
		updateHighlight = null;
	}

	private class UpdateHighlight extends Thread implements
			ViewTreeObserver.OnScrollChangedListener,
			ViewTreeObserver.OnGlobalLayoutListener,
			TextWatcher {

		final int DELAY = 500; // 500ms
		final int SCROLL_DELAY = 500; // 200ms

		boolean isScrollChanged, isTextChanged, isLayoutReady = false;
		long scrollChangedTimestamp = 0;
		private int previousStart = 0;
		private int previousEnd = 0;

		@Override
		public void run() {
			while (true) {
				long timeLapse = SystemClock.elapsedRealtime() - scrollChangedTimestamp;

				// request an highlight after a scroll stopped delay
				if (isScrollChanged && (timeLapse > SCROLL_DELAY)) {
					final int start = getOffsetForPosition(0, scroll.getScrollY());
					final int end = getOffsetForPosition(0, scroll.getScrollY() + scroll.getHeight());
					final int pStart = previousStart;
					final int pEnd = previousEnd;
					post(new Runnable() {
						@Override
						public void run() {
							clearSpans(pStart, pEnd);
							highlight(start, end);
						}
					});
					isScrollChanged = false;
					previousStart = start;
					previousEnd = end;
				}
				// requet an highlight if the text has changed
				if ((isTextChanged) && (isLayoutReady)) {
					int line = getLayout().getLineForOffset(getSelectionStart());
					final int start = getLayout().getLineStart(line);
					final int end = getLayout().getLineEnd(line);
					post(new Runnable() {
						@Override
						public void run() {
						clearSpans(start, end);
						highlight(start, end);
						}
					});
					isTextChanged = false;
				}
				// sleep
				try { Thread.sleep(DELAY); } catch (InterruptedException e) {return;}
			}
		}

		@Override
		public void onScrollChanged() {
			scrollChangedTimestamp = SystemClock.elapsedRealtime();
			isScrollChanged = true;
		}

		@Override
		public void onGlobalLayout() {
			final int start = getOffsetForPosition(0, scroll.getScrollY());
			final int end = getOffsetForPosition(0, scroll.getScrollY() + scroll.getHeight());
			previousStart = start;
			previousEnd = end;
			post(new Runnable() {
				@Override
				public void run() {
					clearSpans(start, end);
					highlight(start, end);
				}
			});
			scroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			isLayoutReady = true;
		}


		@Override
		public void afterTextChanged(Editable editable) {
			isTextChanged = true;
		}
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

	}

	private class IdentFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			if ((end == 1) && (source.charAt(start) == '\n')) {
				StringBuilder ident = new StringBuilder();
				// find the last line
				int n=1;
				while (dest.charAt(dstart-n) != '\n') n++;
				// copy previous ident and autoexpand comments
				int s=1;
				char c = dest.charAt(dstart-n+s++);
				while (c == ' ' || c == '/') {
					ident.append(c);
					c = dest.charAt(dstart-n+s++);
				}
				// extra ident after these chars
				if ("{(*+/-=%".contains(String.valueOf(dest.charAt(dstart-1)))) ident.append("   ");
				return source+ident.toString();
			}
			return source;
		}
	}

	public class ErrorLineSpan implements LineBackgroundSpan {
		int errorLine = 0;

		public ErrorLineSpan(int errorLine) {
			this.errorLine = errorLine;
		}
		@Override
		public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom, CharSequence charSequence, int start, int end, int lineNumber) {

			int color = paint.getColor();
			if ((lineNumber == errorLine) && (errorLine != 0)) {
				paint.setColor(COLOR_ERROR);
				canvas.drawRect(left, top, right, bottom, paint);
			}
			paint.setColor(color);
		}
	}

	public class EditorLineNumberSpan implements LineBackgroundSpan, LeadingMarginSpan {
		int marginText = 0;
		int marginLine = 0;
		Rect bounds = new Rect();


		@Override
		public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom, CharSequence charSequence, int start, int end, int lineNumber) {
			int color = paint.getColor();
			paint.setColor(COLOR_COMMENT);
			String lineNumberStr = String.valueOf(lineNumber+1);
			getPaint().getTextBounds(lineNumberStr,0,lineNumberStr.length(),bounds);
			canvas.drawText(lineNumberStr,marginText-bounds.width(),baseline,paint);
			paint.setColor(color);
		}

		@Override
		public int getLeadingMargin(boolean first) {
			if (first) {
				String lineCount = String.valueOf(ShaderEditor.this.getLineCount());
				getPaint().getTextBounds(lineCount,0,lineCount.length(),bounds);
				marginText = bounds.width();
				getPaint().getTextBounds("xx",0,"xx".length(),bounds);
				marginLine = bounds.width();
			}
			return marginText+marginLine;
		}

		@Override
		public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir, int top, int baseline, int bottom, CharSequence charSequence, int start, int end, boolean first, Layout layout) {
			int color = paint.getColor();
			float width = paint.getStrokeWidth();

			paint.setColor(COLOR_COMMENT);
			paint.setStrokeWidth(1.0f);
			int xpos = marginText+(marginLine/2);
			canvas.drawLine(xpos,top,xpos,bottom,paint);

			paint.setStrokeWidth(width);
			paint.setColor(color);
		}
	}

}
