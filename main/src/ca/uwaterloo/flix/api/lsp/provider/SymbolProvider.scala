/*
 * Copyright 2021 Nicola Dardanis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.uwaterloo.flix.api.lsp.provider

import ca.uwaterloo.flix.api.lsp.{DocumentSymbol, Location, Range, SymbolInformation, SymbolKind}
import ca.uwaterloo.flix.language.ast.TypedAst
import ca.uwaterloo.flix.language.ast.TypedAst.Root
import ca.uwaterloo.flix.language.debug.FormatKind.formatKind

object SymbolProvider {

  /**
    * Returns all symbols in the workspace that match the given query string.
    *
    * As for now we just match the very beginning of the string with the query string,
    * but a more inclusive matching pattern can be implemented.
    */
  def processWorkspaceSymbols(query: String)(implicit root: Root): List[SymbolInformation] = {
    if (root == null) {
      // No AST available.
      return Nil
    }

    val enums = root.enums.values.filter(_.sym.name.startsWith(query)).flatMap(mkEnumSymbolInformation)
    val defs = root.defs.values.collect { case d if d.sym.name.startsWith(query) => mkDefSymbolInformation(d) }
    val classes = root.classes.values.collect { case c if c.sym.name.startsWith(query) => mkClassSymbolInformation(c) }
    val sigs = root.sigs.values.collect { case sig if sig.sym.name.startsWith(query) => mkSigSymbolInformation(sig) }
    (classes ++ defs ++ enums ++ sigs).toList
  }

  /**
    * Returns all symbols that are inside the file pointed by uri.
    */
  def processDocumentSymbols(uri: String)(implicit root: Root): List[DocumentSymbol] = {
    if (root == null) {
      // No AST available.
      return Nil
    }

    val enums = root.enums.values.collect { case enum if enum.loc.source.name == uri => mkEnumDocumentSymbol(enum) }
    val defs = root.defs.values.collect { case d if d.sym.loc.source.name == uri => mkDefDocumentSymbol(d) }
    val classes = root.classes.values.collect { case c if c.sym.loc.source.name == uri => mkClassDocumentSymbol(c) }
    (classes ++ defs ++ enums).toList
  }

  /**
    * Returns an Interface SymbolInformation from a Class node.
    */
  private def mkClassSymbolInformation(c: TypedAst.Class) = c match {
    case TypedAst.Class(_, _, sym, _, _, _, _, _) => SymbolInformation(
      sym.name, SymbolKind.Interface, Nil, deprecated = false, Location(sym.loc.source.name, Range.from(sym.loc)), None,
    )
  }

  /**
    * Returns an Interface DocumentSymbol from a Class node.
    * It navigates the AST and adds Sig and TypeParam of c and as children DocumentSymbols.
    */
  private def mkClassDocumentSymbol(c: TypedAst.Class): DocumentSymbol = c match {
    case TypedAst.Class(doc, _, sym, tparam, _, signatures, _, _) => DocumentSymbol(
      sym.name,
      Some(doc.text),
      SymbolKind.Interface,
      Range.from(sym.loc),
      Range.from(sym.loc),
      Nil,
      signatures.map(mkSigDocumentSymbol) :+ mkTypeParamDocumentSymbol(tparam),
    )
  }

  /**
    * Returns a TypeParameter DocumentSymbol from a TypeParam node.
    */
  private def mkTypeParamDocumentSymbol(t: TypedAst.TypeParam) = t match {
    case TypedAst.TypeParam(name, tpe, loc) => DocumentSymbol(
      name.name, Some(formatKind(tpe.kind)), SymbolKind.TypeParameter, Range.from(loc), Range.from(loc), Nil, Nil,
    )
  }

  /**
    * Returns a Method DocumentSymbol from a Sig node.
    */
  private def mkSigDocumentSymbol(s: TypedAst.Sig): DocumentSymbol = s match {
    case TypedAst.Sig(sym, spec, _) => DocumentSymbol(
      sym.name, Some(spec.doc.text), SymbolKind.Method, Range.from(sym.loc), Range.from(sym.loc), Nil, Nil,
    )
  }

  /**
    * Returns a Method SymbolInformation from a Sig node.
    */
  private def mkSigSymbolInformation(s: TypedAst.Sig): SymbolInformation = s match {
    case TypedAst.Sig(sym, _, _) => SymbolInformation(
      sym.name, SymbolKind.Method, Nil, deprecated = false, Location(sym.loc.source.name, Range.from(sym.loc)), None,
    )
  }

  /**
    * Returns a Function DocumentSymbol from a Def node.
    */
  private def mkDefDocumentSymbol(d: TypedAst.Def): DocumentSymbol = d match {
    case TypedAst.Def(sym, spec, _) => DocumentSymbol(
      sym.name, Some(spec.doc.text), SymbolKind.Function, Range.from(sym.loc), Range.from(sym.loc), Nil, Nil,
    )
  }

  /**
    * Returns a Function SymbolInformation from a Def node.
    */
  private def mkDefSymbolInformation(d: TypedAst.Def): SymbolInformation = d match {
    case TypedAst.Def(sym, _, _) => SymbolInformation(
      sym.name, SymbolKind.Function, Nil, deprecated = false, Location(sym.loc.source.name, Range.from(sym.loc)), None,
    )
  }

  /**
    * Returns an Enum DocumentSymbol from an Enum node.
    * It navigates the AST and adds Cases of enum as children DocumentSymbols.
    */
  private def mkEnumDocumentSymbol(enum: TypedAst.Enum): DocumentSymbol = enum match {
    case TypedAst.Enum(doc, _, sym, tparams, cases, _, _, loc) => DocumentSymbol(
      sym.name,
      Some(doc.text),
      SymbolKind.Enum,
      Range.from(loc),
      Range.from(loc),
      Nil,
      cases.values.map(mkCaseDocumentSymbol).toList ++ tparams.map(mkTypeParamDocumentSymbol),
    )
  }

  /**
    * Returns an EnumMember DocumentSymbol from a Case node.
    */
  private def mkCaseDocumentSymbol(c: TypedAst.Case): DocumentSymbol = c match {
    case TypedAst.Case(_, tag, _, _, loc) => DocumentSymbol(
      tag.name, None, SymbolKind.EnumMember, Range.from(loc), Range.from(loc), Nil, Nil,
    )
  }

  /**
    * Returns an Enum DocumentSymbol from an Enum node.
    * It navigates the AST and returns also the Cases of the enum to the returned List.
    */
  private def mkEnumSymbolInformation(enum: TypedAst.Enum): List[SymbolInformation] = enum match {
    case TypedAst.Enum(_, _, sym, _, cases, _, _, loc) =>
      cases.values.map(mkCaseSymbolInformation).toList :+ SymbolInformation(
          sym.name, SymbolKind.Enum, Nil, deprecated = false, Location(loc.source.name, Range.from(loc)), None,
      )
  }

  /**
    * Returns an EnumMember SymbolInformation from a Case node.
    */
  private def mkCaseSymbolInformation(c: TypedAst.Case): SymbolInformation = c match {
    case TypedAst.Case(_, tag, _, _, loc) => SymbolInformation(
      tag.name, SymbolKind.EnumMember, Nil, deprecated = false, Location(loc.source.name, Range.from(loc)), None)
  }
}
