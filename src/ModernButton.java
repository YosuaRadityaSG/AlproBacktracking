import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class ModernButton extends JButton {
    private boolean hover;
    private boolean press;
    private Color defaultColor;
    private Color hoverColor;
    private Color pressColor;
    private Color textColor;
    private int radius = 10;
    
    public ModernButton(String text, Color defaultColor) {
        super(text);
        this.defaultColor = defaultColor;
        this.hoverColor = adjustColor(defaultColor, 20);
        this.pressColor = adjustColor(defaultColor, -20);
        this.textColor = Color.black;
        setupButton();
    }
    
    public ModernButton(String text) {
        this(text, new Color(63, 81, 181));
    }
    
    private void setupButton() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(textColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                press = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                press = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                press = false;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isEnabled()) {
            if (press) {
                g2.setColor(pressColor);
            } else if (hover) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(defaultColor);
            }
        } else {
            g2.setColor(new Color(60, 60, 60));
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        FontMetrics metrics = g2.getFontMetrics(getFont());
        int textWidth = metrics.stringWidth(getText()), textHeight = metrics.getHeight(), x = (getWidth() - textWidth) / 2, y = (getHeight() - textHeight) / 2 + metrics.getAscent();
        
        g2.setColor(isEnabled() ? textColor : new Color(160, 160, 160));
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
    
    private Color adjustColor(Color color, int amount) {
        int red = Math.max(0, Math.min(255, color.getRed() + amount)), green = Math.max(0, Math.min(255, color.getGreen() + amount)), blue = Math.max(0, Math.min(255, color.getBlue() + amount));

        return new Color(red, green, blue);
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }
    
    public void setDefaultColor(Color color) {
        this.defaultColor = color;
        this.hoverColor = adjustColor(color, 20);
        this.pressColor = adjustColor(color, -20);
        repaint();
    }
    
    public void setTextColor(Color color) {
        this.textColor = color;
        repaint();
    }
}
