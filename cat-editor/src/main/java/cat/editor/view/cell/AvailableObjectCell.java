/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class AvailableObjectCell extends Cell {

    private static final double SIZE = 10;

    public AvailableObjectCell(String id, String name, double x, double y) {
        super(id);

        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 20));
        double height = text.getBoundsInLocal().getHeight();
//        System.out.println(height + " ::: height");
        text.relocate(25, -(height / 2 - SIZE));

        Circle shape = new Circle(SIZE, SIZE, SIZE);
        shape.setUserData("aaa");
        shape.setStroke(CellColors.AVAILABLE_STROKE_COLOR);
        shape.setFill(CellColors.OBJECT_FILL_COLOR);
        shape.setStrokeWidth(3);

        setView(shape);
        setView(text);
        relocate(x, y);
    }

}