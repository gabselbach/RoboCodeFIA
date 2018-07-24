package Deriva;

import robocode.*;
import java.awt.Color;

public class Socorro extends AdvancedRobot{
	
   final double pi = Math.PI;  //Calculos para radios e afins. 

   long inCorner; //Aqui está armazenado um instante de tempo, quando nos chocamos com a parede

   Enemy target; //Objeto inimigo onde o inimigo será atacado.

   int hit = 0; //quantas balas nos atingiram
 
   int direcao = 1; //indica a direção

   double potencia; //potencia bb
   
	void calculoMovimento() {
        //Se somos mais de 300, nos aproximamos com um ângulo de 45 graus. 
        if (target.distance < 300) {
	      // Se um certo tempo passou do último movimento;
            if (getTime() % 20 == 0) { 
                //Nós estabelecemos a direção do movimento
                if (hit < 4) {  // se o numero de tiros recebidos for menor que 4 mudamos a direção
                    direcao*= -1;
                } else {
                    //Se fomos atingidos 4 vezes, descrevemos um arco de circunferência durante um certo tempo
                    if (getTime() % 60 == 0) { //um minuto sem ser atingido
                        hit = 0;
                    }
                }
                setAhead(direcao* (350 +(int)((int) Math.random() * 350))); //mudamos a rota bb;  
            }
            setTurnRightRadians(target.bearing + (pi / 2)); //vamoo armmaaa 
        } else {
            setAhead(300);
            setTurnRightRadians(target.bearing + (pi / 4)); //vamoo armmaaa
        }
    }// fim da funçao



    void escanear() {
	
        double radarOffset;
     
	   if (getTime() - target.ctime > 5) {
            radarOffset = 360;  
        } else { 
            //Calculamos o quanto o radar precisa se mover para apontar para o inimigo GENIAL
            radarOffset = getRadarHeadingRadians() - absbearing(getX(), getY(), target.x, target.y);
            //Porque até que possamos executar esta função, pode mudar para passar algum tempo
            //determinado, vamos adicionar uma pequena quantia ao deslocamento para não perder o inimigo 
            if (radarOffset < 0) {
                radarOffset -= pi / 7;
            } else {
                radarOffset += pi / 7;
            }
        }
        setTurnRadarLeftRadians(NormaliseBearing(radarOffset));// muda a posição do radar 
    }//fim da função


    public void run() {
	
		setColors(Color.BLACK, Color.WHITE, Color.BLACK);//cor 
        
		//Inicializando variável
        inCorner = getTime(); //Retorna o tempo de jogo da rodada atual, onde a hora é igual à curva atual na rodada.
        target = new Enemy(); // instanciando o alvo como objeto inimigo 
		
        target.distance = 900000000; //GAMBIARRA PIQUE ZICA 
		
        //Calculos
        setAdjustGunForRobotTurn(true); //Define a arma para se tornar independente do turno do robô
        setAdjustRadarForGunTurn(true); //Seta o Radar para se tornar independente do turno da arma
        turnRadarRightRadians(2 * pi); //Calculos by Heber e sua IA
        
		while (true) { // um lindo loop infinito 
            
            calculoMovimento(); //Calcula o proximo movimiento 

            calculoPotencia(); //Calcula a potencia de disparo 

            escanear();//Buscamos inimigos 
            apontar(); //apontamos p/ enemigo 
            setFire(potencia);//Calculado, disparamos 
            execute();
        } // bye loop
    } //  bye main
   

    //Metodo para contar o numero de impactos recibidos 
    public void onHitByBullet(HitByBulletEvent event) {
        hit = hit + 1; //ADCIONANDO O NUMERO D VEZES Q FUI BALEADO NA ROCINHA 
    }

    //Metodo por si golpeamos una pared 
    public void onHitWall(HitWallEvent event) {
        //Obtenemos el instante de tiempo en que ha courrido todo; 
        long temp = getTime();
        //Si ha pasado muy poco tiempo desde el ultimo choque 

        if ((temp - inCorner) < 100) {
            //Nos girmos hacia el nemigo y avanzamos hacia el 
            setBack(100);
            setTurnRightRadians(target.bearing);
            execute();
            setAhead(300);
            execute();
        }
        inCorner = temp; //Actualizamos la variable para saber la utlima vez que chocamos con la pared 

    } // fim da função  

    //Calculamos o movimiento do radar para apontar pro inimigo 

    //Metodo para apontar o canhão 

    void apontar() {
     
        long time = getTime() + (int) (target.distance / (20 - (3 * (400 / target.distance))));
      
        double gunOffset = getGunHeadingRadians() - absbearing(getX(), getY(), target.guessX(time), target.guessY(time));
        setTurnGunLeftRadians(NormaliseBearing(gunOffset));
    }
    
    double NormaliseBearing(double ang) {
        if (ang > pi) {
            ang -= 2 * pi;
        }
        if (ang < -pi) {
            ang += 2 * pi;
        }
        return ang;
    }

    double NormaliseHeading(double ang) {
        if (ang > 2 * pi) {
            ang -= 2 * pi;
        }
        if (ang < 0) {
            ang += 2 * pi;
        }
        return ang;
    }
    
    public double distancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
    
    public double absbearing(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = distancia(x1, y1, x2, y2);
        if (xo > 0 && yo > 0) {
            return Math.asin(xo / h);
        }
        if (xo > 0 && yo < 0) {
            return Math.PI - Math.asin(xo / h);
        }
        if (xo < 0 && yo < 0) {
            return Math.PI + Math.asin(-xo / h);
        }
        if (xo < 0 && yo > 0) {
            return 2.0 * Math.PI - Math.asin(-xo / h);
        }
        return 0;
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        System.out.println("Escaneo a :" + e.getName());

        if ((e.getDistance() < target.distance) || (target.name == e.getName())) { // atira e depois 
            //Parametros do inimigo. 
            target.name = e.getName(); // pegando o nome
            target.bearing = e.getBearingRadians();
            target.head = e.getHeadingRadians();
            target.ctime = getTime();
            target.speed = e.getVelocity();
            target.distance = e.getDistance();
            double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * pi);
            target.x = getX() + Math.sin(absbearing_rad) * e.getDistance();
            target.y = getY() + Math.cos(absbearing_rad) * e.getDistance();
        }

    }
    //Método para calcular a potencia de disparo 

    void calculoPotencia() {
        //A potencia e inversamente proporcional a distancia. 
        potencia = 500 / target.distance;
    }
	
    //Quando um robô morre, eu faço minha distância do inimigo muito grande.
    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName() == target.name) {
            target.distance = 9000000;
        }
    }
    

	//Se nos chocamos com um robo, atacamos
    public void onHitRobot(HitRobotEvent event) {   
	    if (event.getName() != target.name) {
            target.distance = 9000000;
        }
    }
}

class Enemy {

    String name;
    public double bearing;
    public double head;
    public long ctime;
    public double speed;
    public double x, y;
    public double distance;

    public String getname() {
        return name;
    }

    public double getbearing() {
        return bearing;
    }

    public double gethead() {
        return head;
    }

    public long getctime() {
        return ctime;
    }

    public double getspeed() {
        return speed;
    }

    public double getx() {
        return x;
    }

    public double gety() {
        return y;
    }

    public double getdistance() {
        return distance;
    }

    public double guessX(long when) {
        long diff = when - ctime;
        return x + Math.sin(head) * speed * diff;
    }

    public double guessY(long when) {
        long diff = when - ctime;
        return y + Math.cos(head) * speed * diff;
    }
}

