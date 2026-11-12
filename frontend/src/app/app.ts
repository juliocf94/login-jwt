import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/** Componente raíz: solo muestra la ruta activa (login o contribuyentes). */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<main><router-outlet /></main>',
})
export class App {}
