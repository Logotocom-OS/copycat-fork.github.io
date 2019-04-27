/**
 * Icon fonts, generated with https://icomoon.io
 *
 * Might be worth trying http://fontello.com/ next time.
 */
import { h } from "preact";

/* tslint:disable:max-line-length */

// FontAwesome "minus-circle"
const noEntry = <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 27 32" class="icon-font">
  <path d="M21.714 17.143v-2.286c0-0.625-0.518-1.143-1.143-1.143h-13.714c-0.625 0-1.143 0.518-1.143 1.143v2.286c0 0.625 0.518 1.143 1.143 1.143h13.714c0.625 0 1.143-0.518 1.143-1.143zM27.429 16c0 7.571-6.143 13.714-13.714 13.714s-13.714-6.143-13.714-13.714 6.143-13.714 13.714-13.714 13.714 6.143 13.714 13.714z"></path>
</svg>;

export const NoEntry = () => noEntry;

// Entypo+ "cog"
const cog = <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" class="icon-font">
  <path d="M26.853 16c0-1.678 1.034-3 2.587-3.909-0.282-0.934-0.651-1.832-1.107-2.675-1.742 0.456-3.152-0.226-4.338-1.413-1.186-1.184-1.549-2.594-1.093-4.338-0.843-0.456-1.741-0.829-2.675-1.106-0.909 1.552-2.552 2.584-4.227 2.584-1.677 0-3.318-1.032-4.229-2.584-0.936 0.277-1.83 0.65-2.674 1.106 0.456 1.744 0.094 3.154-1.094 4.338-1.184 1.187-2.594 1.869-4.338 1.413-0.456 0.843-0.827 1.741-1.106 2.675 1.552 0.909 2.584 2.23 2.584 3.909 0 1.675-1.032 3.318-2.584 4.229 0.28 0.934 0.65 1.83 1.106 2.675 1.744-0.456 3.154-0.094 4.338 1.091 1.186 1.187 1.55 2.597 1.094 4.338 0.843 0.456 1.739 0.829 2.675 1.109 0.909-1.557 2.552-2.587 4.229-2.587 1.675 0 3.318 1.032 4.229 2.587 0.934-0.282 1.83-0.653 2.675-1.109-0.456-1.741-0.094-3.15 1.093-4.338 1.186-1.184 2.595-1.866 4.338-1.413 0.456-0.843 0.827-1.738 1.107-2.675-1.557-0.91-2.59-2.232-2.59-3.907zM16 21.843c-3.229 0-5.845-2.616-5.845-5.843 0-3.229 2.618-5.846 5.845-5.846 3.229 0 5.843 2.619 5.843 5.846 0 3.229-2.614 5.843-5.843 5.843z"></path>
</svg>;

export const Cog = () => cog;

// FontAwesome "switch"
const power = <path d="M27.429 16c0 7.554-6.161 13.714-13.714 13.714s-13.714-6.161-13.714-13.714c0-4.339 2-8.339 5.482-10.946 1.018-0.768 2.446-0.571 3.196 0.446 0.768 1 0.554 2.446-0.446 3.196-2.321 1.75-3.661 4.411-3.661 7.304 0 5.036 4.107 9.143 9.143 9.143s9.143-4.107 9.143-9.143c0-2.893-1.339-5.554-3.661-7.304-1-0.75-1.214-2.196-0.446-3.196 0.75-1.018 2.196-1.214 3.196-0.446 3.482 2.607 5.482 6.607 5.482 10.946zM16 2.286v11.429c0 1.25-1.036 2.286-2.286 2.286s-2.286-1.036-2.286-2.286v-11.429c0-1.25 1.036-2.286 2.286-2.286s2.286 1.036 2.286 2.286z"></path>;

const off = <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 27 32" class="icon-font computer-state-off">
  #{power}
</svg>;

const on = <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 27 32" class="icon-font computer-state-on">
  #{power}
</svg>;

export const Off = () => off;
export const On = () => on;
