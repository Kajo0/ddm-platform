import {HttpErrorResponse} from '@angular/common/http';
import {ErrorHandler, Injectable, NgZone} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor(private snackBar: MatSnackBar,
              private zone: NgZone) {}

  handleError(error: any) {
    console.error('Error from global error handler', error);

    if (!(error instanceof HttpErrorResponse)) {
      error = error.rejection;
    }
    let message = error?.error?.message || error?.message;
    console.log(error);
    console.log(error?.error);
    console.log(error?.error?.message);
    console.log(error?.message);
    if (error?.error) {
      try {
        message = JSON.parse(error.error)?.message;
      } catch (e) {
        console.log('error parsing json error', e);
      }
    }

    this.zone.run(() => this.snackBar.open(message, 'OK'));
  }
}
