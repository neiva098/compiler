/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokens;

/**
 *
 * @author cristiano
 */
public class TagEnums {

    public static int //Palavras reservadas
            PROGRAM = 256,
            DECLARE = 257,
            BEGIN = 258,
            END = 259,
            INT = 260,
            FLOAT = 261,
            CHAR = 262,
            IF = 263,
            THEN = 264,
            ELSE = 265,
            REPEAT = 266,
            UNTIL = 267,
            WHILE = 268,
            DO = 269,
            IN = 270,
            OUT = 271,
            
            //Operadores e pontuação
            ASSIGN,
            EQ = 272,
            GE = 273,
            GT = 274,
            LE = 275,
            LT = 276,
            NE = 277,
            ADD = 278,
            SUB = 279,
            MUL = 280,
            DIV = 281,
            AND = 282,
            OR = 283,
            NOT,
            COMMA,
            SEMICOLON,
            COLON,
            OPEN_BRA,
            CLOSE_BRA,
            DOT,
            //Outros tokens
            
            NUM = 284,
            ID = 285,
            STRING = 286,
            CARACTERE = 287,
            INVALID_TOKEN = 288,
            END_OF_FILE = 289,
            COMMENT = 291,
            UNEXPECTED_EOF = 292;
}
