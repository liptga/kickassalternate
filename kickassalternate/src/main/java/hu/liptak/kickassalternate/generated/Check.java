//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.22 at 10:41:27 DU CEST 
//

package hu.liptak.kickassalternate.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Parameters" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://code.google.com/p/kickassalternate}Parameter" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Include" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://code.google.com/p/kickassalternate}RegexpPattern" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Exclude" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://code.google.com/p/kickassalternate}RegexpPattern" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="className" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="legend" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{ "parameters", "include", "exclude" })
@XmlRootElement(name = "Check")
public class Check
{

	@XmlElement(name = "Parameters")
	protected Check.Parameters parameters;
	@XmlElement(name = "Include")
	protected Check.Include include;
	@XmlElement(name = "Exclude")
	protected Check.Exclude exclude;
	@XmlAttribute(required = true)
	protected String className;
	@XmlAttribute(required = true)
	protected String legend;

	/**
	 * Gets the value of the parameters property.
	 * 
	 * @return possible object is {@link Check.Parameters }
	 * 
	 */
	public Check.Parameters getParameters()
	{
		return parameters;
	}

	/**
	 * Sets the value of the parameters property.
	 * 
	 * @param value
	 *            allowed object is {@link Check.Parameters }
	 * 
	 */
	public void setParameters(Check.Parameters value)
	{
		this.parameters = value;
	}

	/**
	 * Gets the value of the include property.
	 * 
	 * @return possible object is {@link Check.Include }
	 * 
	 */
	public Check.Include getInclude()
	{
		return include;
	}

	/**
	 * Sets the value of the include property.
	 * 
	 * @param value
	 *            allowed object is {@link Check.Include }
	 * 
	 */
	public void setInclude(Check.Include value)
	{
		this.include = value;
	}

	/**
	 * Gets the value of the exclude property.
	 * 
	 * @return possible object is {@link Check.Exclude }
	 * 
	 */
	public Check.Exclude getExclude()
	{
		return exclude;
	}

	/**
	 * Sets the value of the exclude property.
	 * 
	 * @param value
	 *            allowed object is {@link Check.Exclude }
	 * 
	 */
	public void setExclude(Check.Exclude value)
	{
		this.exclude = value;
	}

	/**
	 * Gets the value of the className property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClassName()
	{
		return className;
	}

	/**
	 * Sets the value of the className property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClassName(String value)
	{
		this.className = value;
	}

	/**
	 * Gets the value of the legend property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLegend()
	{
		return legend;
	}

	/**
	 * Sets the value of the legend property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLegend(String value)
	{
		this.legend = value;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{http://code.google.com/p/kickassalternate}RegexpPattern" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder =
	{ "regexpPattern" })
	public static class Exclude
	{

		@XmlElement(name = "RegexpPattern", required = true)
		protected List<RegexpPattern> regexpPattern;

		/**
		 * Gets the value of the regexpPattern property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the regexpPattern property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getRegexpPattern().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link RegexpPattern }
		 * 
		 * 
		 */
		public List<RegexpPattern> getRegexpPattern()
		{
			if (regexpPattern == null)
			{
				regexpPattern = new ArrayList<RegexpPattern>();
			}
			return this.regexpPattern;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{http://code.google.com/p/kickassalternate}RegexpPattern" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder =
	{ "regexpPattern" })
	public static class Include
	{

		@XmlElement(name = "RegexpPattern", required = true)
		protected List<RegexpPattern> regexpPattern;

		/**
		 * Gets the value of the regexpPattern property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the regexpPattern property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getRegexpPattern().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link RegexpPattern }
		 * 
		 * 
		 */
		public List<RegexpPattern> getRegexpPattern()
		{
			if (regexpPattern == null)
			{
				regexpPattern = new ArrayList<RegexpPattern>();
			}
			return this.regexpPattern;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{http://code.google.com/p/kickassalternate}Parameter" maxOccurs="unbounded"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder =
	{ "parameter" })
	public static class Parameters
	{

		@XmlElement(name = "Parameter", required = true)
		protected List<Parameter> parameter;

		/**
		 * Gets the value of the parameter property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the parameter property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getParameter().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link Parameter }
		 * 
		 * 
		 */
		public List<Parameter> getParameter()
		{
			if (parameter == null)
			{
				parameter = new ArrayList<Parameter>();
			}
			return this.parameter;
		}

	}

}
