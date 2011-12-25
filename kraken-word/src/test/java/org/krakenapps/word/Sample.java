package org.krakenapps.word;

import java.io.File;
import java.io.IOException;

import org.krakenapps.word.model.Body;
import org.krakenapps.word.model.Document;
import org.krakenapps.word.model.GridColumnDefinition;
import org.krakenapps.word.model.HexColor;
import org.krakenapps.word.model.LeftBorder;
import org.krakenapps.word.model.Paragraph;
import org.krakenapps.word.model.ParagraphProperties;
import org.krakenapps.word.model.ParagraphStyle;
import org.krakenapps.word.model.Run;
import org.krakenapps.word.model.Table;
import org.krakenapps.word.model.TableBorders;
import org.krakenapps.word.model.TableBottomBorder;
import org.krakenapps.word.model.TableCell;
import org.krakenapps.word.model.TableCellBorders;
import org.krakenapps.word.model.TableCellProperties;
import org.krakenapps.word.model.TableGrid;
import org.krakenapps.word.model.TableLeadingEdgeBorder;
import org.krakenapps.word.model.TableProperties;
import org.krakenapps.word.model.TableRow;
import org.krakenapps.word.model.TableTopBorder;
import org.krakenapps.word.model.TableTrailingEdgeBorder;
import org.krakenapps.word.model.TableWidth;
import org.krakenapps.word.model.Text;
import org.krakenapps.word.model.TwipsMeasure;

public class Sample {

	public static void main(String[] args) throws IOException {
		WordFile f = new WordFile(new File("qoo.docx"));
		f.setDoc(dummy());
		f.write();
	}

	private static Document dummy() {
		Document d = new Document();
		Body b = new Body();
		b.add(newTextParagraph("방화벽 차단 통계", "Heading1"));
		b.add(newTextParagraph("지난 1주일간 방화벽에서 접속을 차단한 세션의 출발지 IP, 목적지 IP별 통계를 표시합니다.", null));
		b.add(newTable());
		d.setBody(b);
		return d;
	}

	private static Paragraph newTextParagraph(String value, String style) {
		Paragraph p = new Paragraph();
		ParagraphProperties pPr = new ParagraphProperties();
		if (style != null)
			pPr.add(new ParagraphStyle(style));

		Run r = new Run();
		Text t = new Text(value);
		r.add(t);
		p.add(pPr);
		p.add(r);
		return p;
	}

	private static Table newTable() {
		Table t = new Table();

		TableProperties tp = new TableProperties();
		TableBorders borders = new TableBorders();
		borders.add(new TableTopBorder("single", 6, 0, new HexColor("000000")));
		borders.add(new TableBottomBorder("single", 6, 0, new HexColor("000000")));
		borders.add(new TableLeadingEdgeBorder("single", 6, 0, new HexColor("000000")));
		borders.add(new TableTrailingEdgeBorder("single", 6, 0, new HexColor("000000")));
		tp.add(borders);
		tp.add(new TableWidth("dxa", 8000));

		TableGrid grid = new TableGrid();
		grid.add(new GridColumnDefinition(new TwipsMeasure(3500)));
		grid.add(new GridColumnDefinition(new TwipsMeasure(3500)));
		grid.add(new GridColumnDefinition(new TwipsMeasure(1000)));

		TableRow tr = new TableRow();
		TableCellProperties tcPr = new TableCellProperties();
		TableCellBorders tcBorders = new TableCellBorders();
		tcBorders.add(new LeftBorder("single", 4, 0, new HexColor("auto")));
		tcPr.add(tcBorders);

		TableCell tc = new TableCell();
		tc.add(newTextParagraph("출발지 IP", null));
		tr.add(tc);

		TableCell tc2 = new TableCell();
		tc2.add(tcPr);
		tc2.add(newTextParagraph("목적지 IP", null));
		tr.add(tc2);

		TableCell tc3 = new TableCell();
		tc3.add(tcPr);
		tc3.add(newTextParagraph("횟수", null));
		tr.add(tc3);

		t.add(tp);
		t.add(grid);
		t.add(tr);
		return t;
	}

}
