import tkinter as tk
from tkinter import messagebox

# Stałe kolory dla planszy
KOLOR_JASNY = "#F0D9B5"
KOLOR_CIEMNY = "#B58863"
KOLOR_ZAZNACZENIA = "#7B9E5A"
KOLOR_MOZLIWEGO_RUCHU = "#C94545"

class SzachyZBossem:
    def __init__(self, root):
        self.root = root
        self.root.title("Szachy: Boss i Urzędnik")
        
        # Inicjalizacja stanu gry
        self.tura = 'white'
        self.wybrane_pole = None
        self.mozliwe_ruchy = []
        
        # Reprezentacja figur na planszy
        # Wielkie litery = Białe, Małe litery = Czarne
        # P/p = Pion, R/r = Wieża, N/n = Skoczek, B/b = Goniec, Q/q = Hetman, K/k = Król
        # W/w = Boss (Szef), U/u = Urzędnik
        self.plansza = [
            ['r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'],
            ['p', 'p', 'p', 'w', 'u', 'p', 'p', 'p'], # Czarny Boss (d7) i Urzędnik (e7)
            ['.', '.', '.', '.', '.', '.', '.', '.'],
            ['.', '.', '.', '.', '.', '.', '.', '.'],
            ['.', '.', '.', '.', '.', '.', '.', '.'],
            ['.', '.', '.', '.', '.', '.', '.', '.'],
            ['P', 'P', 'P', 'W', 'U', 'P', 'P', 'P'], # Biały Boss (d2) i Urzędnik (e2)
            ['R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R']
        ]

        # Słownik do ładnego wyświetlania figur
        self.znaki_figur = {
            'R': '♖', 'N': '♘', 'B': '♗', 'Q': '♕', 'K': '♔', 'P': '♙',
            'W': 'B', 'U': 'U', # Zwykłe litery dla Bossa i Urzędnika by się wyróżniały
            'r': '♜', 'n': '♞', 'b': '♝', 'q': '♛', 'k': '♚', 'p': '♟',
            'w': 'b', 'u': 'u',
            '.': ''
        }
        
        self.tworz_ui()
        self.odswiez_plansze()

    def tworz_ui(self):
        self.ramka_planszy = tk.Frame(self.root)
        self.ramka_planszy.pack(padx=20, pady=20)
        
        self.etykieta_tury = tk.Label(self.root, text="Tura: BIAŁE", font=("Arial", 16, "bold"))
        self.etykieta_tury.pack(pady=10)

        self.przyciski = [[None for _ in range(8)] for _ in range(8)]
        
        for r in range(8):
            for c in range(8):
                kolor_tla = KOLOR_JASNY if (r + c) % 2 == 0 else KOLOR_CIEMNY
                btn = tk.Button(self.ramka_planszy, text="", font=("Arial", 28), 
                                width=3, height=1, bg=kolor_tla, 
                                activebackground=KOLOR_ZAZNACZENIA,
                                command=lambda row=r, col=c: self.klikniecie_pola(row, col))
                btn.grid(row=r, column=c)
                self.przyciski[r][c] = btn

    def odswiez_plansze(self):
        for r in range(8):
            for c in range(8):
                figura = self.plansza[r][c]
                tekst = self.znaki_figur.get(figura, '')
                
                # Ustawienie kolorów tekstu dla widoczności
                if figura.isupper():
                    kolor_tekstu = "white" if figura in ['W', 'U'] else "black" # Wyróżnienie Bossa/Urzędnika
                elif figura.islower():
                    kolor_tekstu = "black" if figura in ['w', 'u'] else "black"
                else:
                    kolor_tekstu = "black"
                
                # Dodatkowe wyróżnienie Bossa i Urzędnika
                font = ("Arial", 24, "bold") if figura.lower() in ['w', 'u'] else ("Arial", 28)

                kolor_tla = KOLOR_JASNY if (r + c) % 2 == 0 else KOLOR_CIEMNY
                
                # Zaznaczone pole
                if self.wybrane_pole == (r, c):
                    kolor_tla = KOLOR_ZAZNACZENIA
                # Możliwe ruchy
                elif (r, c) in self.mozliwe_ruchy:
                    kolor_tla = KOLOR_MOZLIWEGO_RUCHU

                self.przyciski[r][c].config(text=tekst, bg=kolor_tla, fg=kolor_tekstu, font=font)
        
        tekst_tury = "BIAŁE" if self.tura == 'white' else "CZARNE"
        kolor_tury = "gray" if self.tura == 'white' else "black"
        self.etykieta_tury.config(text=f"Tura: {tekst_tury}", fg=kolor_tury)

    def czy_figura_gracza(self, r, c):
        figura = self.plansza[r][c]
        if figura == '.': return False
        if self.tura == 'white': return figura.isupper()
        else: return figura.islower()

    def czy_wrog_ma_urzednika(self, kolor_gracza):
        # Sprawdza czy na planszy jest urzędnik przeciwnika
        cel = 'u' if kolor_gracza == 'white' else 'U'
        for wiersz in self.plansza:
            if cel in wiersz:
                return True
        return False

    def klikniecie_pola(self, r, c):
        if self.wybrane_pole:
            # Próba ruchu
            if (r, c) in self.mozliwe_ruchy:
                self.wykonaj_ruch(self.wybrane_pole, (r, c))
                self.wybrane_pole = None
                self.mozliwe_ruchy = []
                self.zmien_ture()
            elif self.czy_figura_gracza(r, c):
                # Zmiana zaznaczenia na inną własną figurę
                self.wybrane_pole = (r, c)
                self.mozliwe_ruchy = self.generuj_ruchy(r, c)
            else:
                # Odznaczenie
                self.wybrane_pole = None
                self.mozliwe_ruchy = []
        else:
            # Zaznaczenie figury
            if self.czy_figura_gracza(r, c):
                self.wybrane_pole = (r, c)
                self.mozliwe_ruchy = self.generuj_ruchy(r, c)
        
        self.odswiez_plansze()

    def wykonaj_ruch(self, start, koniec):
        sr, sc = start
        kr, kc = koniec
        figura = self.plansza[sr][sc]
        zbita_figura = self.plansza[kr][kc]
        
        # Wykonanie ruchu
        self.plansza[kr][kc] = figura
        self.plansza[sr][sc] = '.'

        # Promocja Piona i Urzędnika po dojściu do końca planszy
        if figura.lower() in ['p', 'u']:
            if (figura.isupper() and kr == 0) or (figura.islower() and kr == 7):
                self.plansza[kr][kc] = 'Q' if figura.isupper() else 'q'

        # Sprawdzenie wygranej (czy zbito króla)
        if zbita_figura.lower() == 'k':
            zwyciezca = "BIAŁE" if self.tura == 'white' else "CZARNE"
            self.odswiez_plansze()
            messagebox.showinfo("Koniec gry!", f"{zwyciezca} wygrywają grę przez zbicie króla!")
            self.root.quit()

    def zmien_ture(self):
        self.tura = 'black' if self.tura == 'white' else 'white'

    def wektory_linii(self, r, c, wektory, max_dystans=7):
        ruchy = []
        moj_kolor = 'white' if self.tura == 'white' else 'black'
        for dr, dc in wektory:
            for i in range(1, max_dystans + 1):
                nr, nc = r + dr * i, c + dc * i
                if 0 <= nr < 8 and 0 <= nc < 8:
                    cel = self.plansza[nr][nc]
                    if cel == '.':
                        ruchy.append((nr, nc))
                    else:
                        cel_kolor = 'white' if cel.isupper() else 'black'
                        if cel_kolor != moj_kolor:
                            ruchy.append((nr, nc)) # Bicie wroga
                        break # Blokada o własną lub wrogą figurę (po biciu)
                else:
                    break
        return ruchy

    def generuj_ruchy(self, r, c):
        figura = self.plansza[r][c]
        moj_kolor = 'white' if figura.isupper() else 'black'
        ruchy = []
        
        litera = figura.lower()

        # ==========================================
        # ZASADY SPECJALNE: BOSS (W/w)
        # ==========================================
        if litera == 'w':
            # Jeśli wrogi Urzędnik żyje, Boss jest zablokowany!
            if self.czy_wrog_ma_urzednika(moj_kolor):
                return [] 
            
            # W przeciwnym razie - Boss może stanąć WSZĘDZIE (gdzie nie ma własnej figury)
            for nr in range(8):
                for nc in range(8):
                    if nr == r and nc == c: continue
                    cel = self.plansza[nr][nc]
                    if cel == '.':
                        ruchy.append((nr, nc))
                    else:
                        cel_kolor = 'white' if cel.isupper() else 'black'
                        if cel_kolor != moj_kolor:
                            ruchy.append((nr, nc)) # Może zbić dowolną figurę wroga
            return ruchy

        # ==========================================
        # ZASADY SPECJALNE: URZĘDNIK (U/u) i PION (P/p)
        # ==========================================
        if litera == 'p' or litera == 'u':
            kierunek = -1 if moj_kolor == 'white' else 1
            rzad_startowy = 6 if moj_kolor == 'white' else 1

            # Ruch do przodu o 1
            if 0 <= r + kierunek < 8 and self.plansza[r + kierunek][c] == '.':
                ruchy.append((r + kierunek, c))
                # Ruch o 2 z rzędu startowego
                if r == rzad_startowy and self.plansza[r + 2 * kierunek][c] == '.':
                    ruchy.append((r + 2 * kierunek, c))

            # Bicie po skosie
            for dc in [-1, 1]:
                if 0 <= r + kierunek < 8 and 0 <= c + dc < 8:
                    cel = self.plansza[r + kierunek][c + dc]
                    if cel != '.':
                        cel_kolor = 'white' if cel.isupper() else 'black'
                        if cel_kolor != moj_kolor:
                            ruchy.append((r + kierunek, c + dc))
            return ruchy

        # ==========================================
        # FIGURY STANDARDOWE
        # ==========================================
        if litera == 'r': # Wieża
            wektory = [(0,1), (0,-1), (1,0), (-1,0)]
            return self.wektory_linii(r, c, wektory)
        
        elif litera == 'b': # Goniec
            wektory = [(1,1), (1,-1), (-1,1), (-1,-1)]
            return self.wektory_linii(r, c, wektory)
            
        elif litera == 'q': # Hetman
            wektory = [(0,1), (0,-1), (1,0), (-1,0), (1,1), (1,-1), (-1,1), (-1,-1)]
            return self.wektory_linii(r, c, wektory)
            
        elif litera == 'k': # Król
            wektory = [(0,1), (0,-1), (1,0), (-1,0), (1,1), (1,-1), (-1,1), (-1,-1)]
            return self.wektory_linii(r, c, wektory, max_dystans=1)
            
        elif litera == 'n': # Skoczek
            skoki = [(-2,-1), (-2,1), (-1,-2), (-1,2), (1,-2), (1,2), (2,-1), (2,1)]
            for dr, dc in skoki:
                nr, nc = r + dr, c + dc
                if 0 <= nr < 8 and 0 <= nc < 8:
                    cel = self.plansza[nr][nc]
                    if cel == '.':
                        ruchy.append((nr, nc))
                    else:
                        cel_kolor = 'white' if cel.isupper() else 'black'
                        if cel_kolor != moj_kolor:
                            ruchy.append((nr, nc))
            return ruchy

        return ruchy

if __name__ == "__main__":
    okno = tk.Tk()
    gra = SzachyZBossem(okno)
    okno.mainloop()